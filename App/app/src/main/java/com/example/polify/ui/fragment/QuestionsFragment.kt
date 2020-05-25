package com.example.polify.ui.fragment

import android.animation.Animator
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.api.load
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.Question
import com.andruid.magic.game.model.response.Result
import com.example.polify.R
import com.example.polify.data.BATTLE_MULTIPLAYER
import com.example.polify.data.BATTLE_ONE_VS_ONE
import com.example.polify.data.BATTLE_TEST
import com.example.polify.data.QUE_TIME_LIMIT_MS
import com.example.polify.databinding.FragmentQuestionsBinding
import com.example.polify.eventbus.OptionEvent
import com.example.polify.ui.adapter.QuestionAdapter
import com.example.polify.ui.viewholder.OptionViewHolder
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.QuestionViewModel
import com.example.polify.util.setOnSoundClickListener
import com.example.polify.util.showConfirmationDialog
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class QuestionsFragment : Fragment() {
    companion object {
        private val TAG = "${QuestionsFragment::class.java.simpleName}Log"
    }

    private val questionsAdapter = QuestionAdapter()
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val questionsViewModel by viewModels<QuestionViewModel> {
        BaseViewModelFactory {
            QuestionViewModel(battle, battleType)
        }
    }

    private val args by navArgs<QuestionsFragmentArgs>()
    private val battle by lazy { args.battle }
    private val battleType by lazy { args.battleType }
    private val startTime by lazy { args.startTime }
    private var currentVolume = 0F

    private lateinit var binding: FragmentQuestionsBinding

    private var finished = false
    private var score = 0
    private var qid: String? = null
    private var selectedOptPos = -1
    private var optionsEnabled = true

    private val exoPlayer by lazy {
        SimpleExoPlayer.Builder(requireContext())
                .build()
    }
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            lifecycleScope.launch {
                val shouldQuit = requireContext().showConfirmationDialog(R.string.title_leave_battle,
                        R.string.desc_leave_battle)
                if (!shouldQuit)
                    return@launch

                requireActivity().finish()
            }
        }
    }
    private val countDownTimer = object : CountDownTimer(QUE_TIME_LIMIT_MS, 1000) {
        override fun onFinish() {
            val pos = binding.viewPager.currentItem

            exoPlayer.playWhenReady = false

            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                Log.d("queLog", "app in foreground")
                showCorrectWrongAnim(pos) {
                    Log.d("qLog", "anim completed for $pos")
                    moveToNextQuestion(pos)
                }
            } else {
                Log.d("queLog", "app in background")
                moveToNextQuestion(pos)
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            binding.timerAnimView.progress = 1 - millisUntilFinished.toFloat() / QUE_TIME_LIMIT_MS
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC

        initExoPlayer()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentQuestionsBinding.inflate(inflater, container, false)

        initViewPager()
        initListeners()
        initPlayers()

        questionsViewModel.questions.observe(viewLifecycleOwner, Observer { result ->
            if (result.status == Result.Status.SUCCESS) {
                (result.data?.questions)?.let { questions ->
                    questionsAdapter.submitList(questions) {
                        Log.d("qLog", "number of questions = ${questions.size}")
                        startMatch(questions)
                    }
                }
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        if (!finished)
            exoPlayer.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
        exoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy questions fragment")

        countDownTimer.cancel()
        exoPlayer.release()
        binding.answerAnimView.removeAllAnimatorListeners()

        val user = mAuth.currentUser ?: return

        if (!finished && battleType != BATTLE_TEST) {
            lifecycleScope.launch {
                val result = GameRepository.leaveBattle(user.uid, battle!!.battleId)
                if (result.status == Result.Status.SUCCESS) {
                    if (result.data?.success == true)
                        Log.d(TAG, "onDestroy: battle left")
                    else
                        Log.d(TAG, "onDestroy: battle left fail")
                } else
                    Log.d(TAG, "onDestroy: battle left")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOptionEvent(optionEvent: OptionEvent) {
        if (!optionsEnabled)
            return

        val (qid, opt) = optionEvent

        this.qid = qid
        Log.d("optionLog", "selected opt = ${opt.optId}")
        selectedOptPos = opt.optId[0] - 'A'

        val optionsRV = binding.viewPager.findViewById<RecyclerView>(R.id.optionsRV)
        val selectedViewHolder = optionsRV.findViewHolderForItemId(selectedOptPos.toLong()) as OptionViewHolder?
        selectedViewHolder?.highlightOption()

        optionsEnabled = false
    }

    private fun initListeners() {
        binding.muteBtn.setOnSoundClickListener {
            if (exoPlayer.volume == 0F) {
                binding.muteBtn.load(R.drawable.sound)
                muteAudio(false)
            }
            else {
                binding.muteBtn.load(R.drawable.no_sound)
                muteAudio(true)
            }
        }
    }

    private fun initExoPlayer() {
        exoPlayer.apply {
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_GAME)
                    .setContentType(C.CONTENT_TYPE_SONIFICATION)
                    .build()
            setAudioAttributes(audioAttributes, false)
            setHandleAudioBecomingNoisy(true)
            setHandleWakeLock(true)

            val uri = RawResourceDataSource.buildRawResourceUri(R.raw.clock)
            val dataSource = RawResourceDataSource(requireContext())
            dataSource.open(DataSpec(uri))
            val mediaSource = ProgressiveMediaSource.Factory(DataSource.Factory { dataSource })
                    .createMediaSource(uri)
            val loopingMediaSource = LoopingMediaSource(mediaSource)

            prepare(loopingMediaSource, false, false)
        }
    }

    private fun initPlayers() {
        val currentUid = mAuth.currentUser?.uid ?: return

        val players = battle?.players ?: emptyList()

        val player = players.find { player -> player.uid == currentUid }
        val remaining = players.minus(player)

        binding.apply {
            player1 = player
            player2 = remaining.getOrNull(0)
            player3 = remaining.getOrNull(1)
            player4 = remaining.getOrNull(2)
            executePendingBindings()
        }
    }

    private fun initViewPager() {
        binding.viewPager.apply {
            adapter = questionsAdapter
            isUserInputEnabled = false

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    Log.d("qLog", "viewpager page position = $position")

                    binding.progressQues.progress = position + 1

                    qid = questionsAdapter.currentList[position].qid

                    optionsEnabled = true

                    countDownTimer.cancel()
                    countDownTimer.start()

                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
                        exoPlayer.playWhenReady = true

                    binding.timerAnimView.playAnimation()
                }
            })
        }
    }

    private fun startMatch(questions: List<Question>) {
        binding.progressQues.max = questions.size

        if (startTime == -1L)
            return

        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
        val questionPos = elapsedSeconds / questions.size

        Log.d("queLog", "questionPos = $questionPos")

        if (questionPos < questions.size)
            binding.viewPager.currentItem = questionPos.toInt() - 1
        else {
            binding.progressQues.progress = binding.progressQues.max
            binding.timerAnimView.progress = 100F
            finishGame()
        }
    }

    private fun moveToNextQuestion(pos: Int) {
        Log.d("queLog", "finished que $pos")

        val question = questionsAdapter.currentList[pos]
        val correctPos = question.correctAnswer[0] - 'A'

        if (qid == question.qid && selectedOptPos == correctPos)
            score++

        selectedOptPos = -1

        if (pos == questionsAdapter.itemCount - 1)
            finishGame()
        else
            binding.viewPager.setCurrentItem(pos + 1, true)
    }

    private fun showCorrectWrongAnim(questionPos: Int, next: () -> Unit) {
        val question = questionsAdapter.currentList[questionPos]

        if (qid == question.qid) {
            Log.d("queLog", "selected option pos = $selectedOptPos, playing anim")
            val correctPos = question.correctAnswer[0] - 'A'

            if (selectedOptPos != correctPos) {
                val optionsRV = binding.viewPager.findViewById<RecyclerView>(R.id.optionsRV)
                val correctViewHolder = optionsRV?.findViewHolderForItemId(correctPos.toLong()) as OptionViewHolder?
                correctViewHolder?.highlightAnswer(true)
            }

            binding.answerAnimView.apply {
                setAnimation(
                        if (selectedOptPos == correctPos)
                            R.raw.correct
                        else
                            R.raw.wrong
                )
                addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        visibility = View.GONE
                        next()
                        removeAnimatorListener(this)
                    }

                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                })
                visibility = View.VISIBLE
                playAnimation()
            }
        }
    }

    private fun finishGame() {
        finished = true
        try {
            when (battleType) {
                BATTLE_ONE_VS_ONE ->
                    findNavController().navigate(QuestionsFragmentDirections
                            .actionQuestionsFragmentToResultsFragment(battle?.battleId
                                    ?: "test", score))
                BATTLE_TEST ->
                    findNavController().navigate(QuestionsFragmentDirections
                            .actionQuestionsFragmentToResultsFragment(score = score))
                BATTLE_MULTIPLAYER ->
                    findNavController().navigate(QuestionsFragmentDirections
                            .actionQuestionsFragmentToResultsFragment(battle?.battleId
                                    ?: "test", score))
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun muteAudio(mute: Boolean = false) {
        if (mute) {
            currentVolume = exoPlayer.volume
            exoPlayer.volume = 0F
        }
        else
            exoPlayer.volume = currentVolume
    }
}