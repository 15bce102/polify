package com.example.polify.ui.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.polify.R
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

    private val countDownTimer = object : CountDownTimer(QUE_TIME_LIMIT_MS, 1000) {
        override fun onFinish() {
            val pos = binding.viewPager.currentItem
            highlightAns(pos)

            lifecycleScope.launch {
                delay(1000)
                if (pos == questionsAdapter.itemCount - 1) {
                    Toast.makeText(requireContext(), "Your score = $score/10!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(QuestionsFragmentDirections.actionQuestionsFragmentToResultsFragment())
                }
                else
                    binding.viewPager.setCurrentItem(pos + 1, true)
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            Log.d(TAG, "${millisUntilFinished / 1000} sec for question")
        }
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
        highlightOption()
    }

    private fun highlightOption() {
        val optionRV = binding.viewPager.findViewById<RecyclerView>(R.id.optionsRV)

        Log.d("optionLog", "selected option = $selectedOptPos")

        val optViewHolder = optionRV.findViewHolderForItemId(selectedOptPos.toLong()) as OptionViewHolder
        optViewHolder.highlightOption(true)
    }

    private fun highlightAns(pos: Int) {
        val question = questionsAdapter.currentList[pos]
        if (qid == question.qid) {
            Log.d(TAG, "selected option pos = $selectedOptPos")
            val correctPos = question.correctAnswer[0] - 'A'
            val optionRV = binding.viewPager.findViewById<RecyclerView>(R.id.optionsRV)

            val correctViewHolder = optionRV.findViewHolderForAdapterPosition(correctPos) as OptionViewHolder
            correctViewHolder.highlightAnswer(true)

            if (selectedOptPos == -1)
                return

            val selViewHolder = optionRV.findViewHolderForAdapterPosition(selectedOptPos) as OptionViewHolder
            if (selectedOptPos == correctPos) {
                selViewHolder.highlightAnswer(true)
                score++
            } else
                selViewHolder.highlightAnswer(false)
        }
    }
}