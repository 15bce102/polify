package com.example.polify.ui.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.andruid.magic.game.model.data.Question
import com.andruid.magic.game.model.response.Result
import com.example.polify.R
import com.example.polify.data.BATTLE_TEST
import com.example.polify.data.QUE_TIME_LIMIT_MS
import com.example.polify.databinding.FragmentQuestionsBinding
import com.example.polify.eventbus.OptionEvent
import com.example.polify.ui.adapter.QuestionAdapter
import com.example.polify.ui.viewholder.OptionViewHolder
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.QuestionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.toast.toast

class QuestionsFragment : Fragment() {
    companion object {
        private val TAG = "${QuestionsFragment::class.java.simpleName}Log"
    }

    private val questionsAdapter = QuestionAdapter()
    private val questionsViewModel by viewModels<QuestionViewModel> {
        BaseViewModelFactory {
            QuestionViewModel(battleId, battleType)
        }
    }

    private lateinit var battleId: String
    private lateinit var binding: FragmentQuestionsBinding

    private var battleType = BATTLE_TEST
    private var score = 0
    private var qid: String? = null
    private var selectedOptPos = -1
    private var startTime = -1L
    private var optionsEnabled = true

    private val countDownTimer = object : CountDownTimer(QUE_TIME_LIMIT_MS, 1000) {
        override fun onFinish() {
            val pos = binding.viewPager.currentItem
            val optionsRV = binding.viewPager.findViewById<RecyclerView>(R.id.optionsRV)
            highlightAns(optionsRV, pos)

            lifecycleScope.launch {
                delay(1000)
                if (pos == questionsAdapter.itemCount - 1) {
                    finishGame()
                } else
                    binding.viewPager.setCurrentItem(pos + 1, true)
            }
        }

        override fun onTick(millisUntilFinished: Long) {}
    }

    private fun finishGame() {
        toast("Your score = $score/10!")
        try {
            findNavController().navigate(
                    QuestionsFragmentDirections.actionQuestionsFragmentToResultsFragment(battleId, battleType, score))
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("cloudLog", "on create questions")
        super.onCreate(savedInstanceState)

        arguments?.let {
            val safeArgs = QuestionsFragmentArgs.fromBundle(it)

            battleId = safeArgs.battleId
            startTime = safeArgs.startTime
            battleType = safeArgs.battleType
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentQuestionsBinding.inflate(inflater, container, false)

        binding.viewPager.apply {
            adapter = questionsAdapter
            isUserInputEnabled = false
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.barProgressBar.progress = position + 1

                    qid = questionsAdapter.currentList[position].qid

                    optionsEnabled = true

                    countDownTimer.cancel()
                    countDownTimer.start()
                    binding.timerAnimView.playAnimation()
                }
            })
        }

        questionsViewModel.questions.observe(viewLifecycleOwner, Observer { result ->
            if (result.status == Result.Status.SUCCESS) {
                (result.data as List<*>?)?.map { q -> q as Question }?.let { questions ->
                    questionsAdapter.submitList(questions)
                    binding.barProgressBar.max = questions.size
                    startMatch(questions)
                }
            }
        })

        return binding.root
    }

    private fun startMatch(questions: List<Question>) {
        if (startTime == -1L)
            return

        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
        val questionPos = elapsedSeconds / questions.size

        Log.d("cloudLog", "questionPos = $questionPos")

        if (questionPos < questions.size)
            binding.viewPager.currentItem = questionPos.toInt() - 1
        else {
            binding.timerAnimView.progress = 100F
            finishGame()
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("cloudLog", "onDestroy questions fragment")
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

    private fun highlightAns(recyclerView: RecyclerView, pos: Int) {
        val question = questionsAdapter.currentList[pos]

        if (qid == question.qid) {
            Log.d(TAG, "selected option pos = $selectedOptPos")
            val correctPos = question.correctAnswer[0] - 'A'

            val correctViewHolder = recyclerView.findViewHolderForItemId(correctPos.toLong()) as OptionViewHolder?
            correctViewHolder?.highlightAnswer(true)

            if (selectedOptPos == -1)
                return

            if (selectedOptPos == correctPos)
                score++
            else {
                val wrongViewHolder = recyclerView.findViewHolderForItemId(selectedOptPos.toLong()) as OptionViewHolder?
                wrongViewHolder?.highlightAnswer(false)
            }

            selectedOptPos = -1
        }
    }
}