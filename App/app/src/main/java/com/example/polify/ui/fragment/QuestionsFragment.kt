package com.example.polify.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.andruid.magic.game.api.GameRepository
import com.example.polify.R
import com.example.polify.data.QUE_TIME_LIMIT_MS
import com.example.polify.databinding.FragmentQuestionsBinding
import com.example.polify.eventbus.OptionEvent
import com.example.polify.ui.adapter.QuestionAdapter
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.QuestionViewModel
import com.example.polify.util.getViewByPosition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class QuestionsFragment : Fragment() {
    companion object {
        private val TAG = "${QuestionsFragment::class.java.simpleName}Log"
    }

    private val questionsAdapter = QuestionAdapter()
    private val questionsViewModel by viewModels<QuestionViewModel> {
        BaseViewModelFactory {
            QuestionViewModel(battleId)
        }
    }

    private lateinit var battleId: String
    private lateinit var binding: FragmentQuestionsBinding

    private var score = 0
    private var qid: String? = null
    private var selectedOptPos = -1

    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val countDownTimer = object : CountDownTimer(QUE_TIME_LIMIT_MS, 1000) {
        override fun onFinish() {
            val pos = binding.viewPager.currentItem
            val optionsLV = binding.viewPager.findViewById<ListView>(R.id.optionsLV)
            highlightAns(optionsLV, pos)

            lifecycleScope.launch {
                delay(1000)
                if (pos == questionsAdapter.itemCount - 1) {
                    Toast.makeText(requireContext(), "Your score = $score/10!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(
                            QuestionsFragmentDirections.actionQuestionsFragmentToResultsFragment(battleId, score))
                } else
                    binding.viewPager.setCurrentItem(pos + 1, true)
            }
        }

        override fun onTick(millisUntilFinished: Long) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            battleId = QuestionsFragmentArgs.fromBundle(it).battleId
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
                    countDownTimer.cancel()
                    countDownTimer.start()
                    binding.timerAnimView.playAnimation()
                }
            })
        }

        questionsViewModel.questions.observe(viewLifecycleOwner, Observer {
            questionsAdapter.submitList(it)
            binding.barProgressBar.max = it.size
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOptionEvent(optionEvent: OptionEvent) {
        val (qid, opt) = optionEvent

        this.qid = qid
        Log.d("optionLog", "selected opt = ${opt.optId}")
        selectedOptPos = opt.optId[0] - 'A'

        val optionsLV = binding.viewPager.findViewById<ListView>(R.id.optionsLV)
        highlightOption(optionsLV, selectedOptPos)
    }

    private fun highlightOption(listView: ListView, pos: Int) {
        listView.getViewByPosition(pos)?.findViewById<LinearLayout>(R.id.linearLayout)?.setBackgroundColor(Color.LTGRAY)
    }

    private fun highlightAns(listView: ListView, pos: Int) {
        val question = questionsAdapter.currentList[pos]

        if (qid == question.qid) {
            Log.d(TAG, "selected option pos = $selectedOptPos")
            val correctPos = question.correctAnswer[0] - 'A'
            highlightAnswer(listView, correctPos, true)

            if (selectedOptPos == -1)
                return

            if (selectedOptPos == correctPos)
                score++
            else
                highlightAnswer(listView, selectedOptPos, false)
        }
    }

    private fun highlightAnswer(listView: ListView, pos: Int, correct: Boolean) {
        val bg = if (correct) Color.GREEN else Color.RED
        listView.getViewByPosition(pos)?.findViewById<LinearLayout>(R.id.linearLayout)?.setBackgroundColor(bg)
    }
}