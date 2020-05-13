package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.polify.R
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
    private var selectedOptId: String? = null

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
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.barProgressBar.progress = position + 1
                }
            })
        }

        binding.nextBtn.setOnClickListener {
            val pos = binding.viewPager.currentItem
            highlightAns(pos)

            lifecycleScope.launch {
                delay(1000)
                if (pos == questionsAdapter.itemCount - 1)
                    Toast.makeText(requireContext(), "Your score = $score/10!", Toast.LENGTH_SHORT).show()
                else
                    binding.viewPager.setCurrentItem(pos + 1, true)
            }
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

        highlightOption(opt.optId)
    }

    private fun highlightOption(optId: String) {
        val optionRV = binding.viewPager.findViewById<RecyclerView>(R.id.optionsRV)
        val pos = optId[0]-'A'

        selectedOptId?.let {
            val prevPos = it[0] - 'A'
            val prevOptViewHolder = optionRV.findViewHolderForAdapterPosition(prevPos) as OptionViewHolder
            prevOptViewHolder.highlightOption(false)
        }

        val optViewHolder = optionRV.findViewHolderForAdapterPosition(pos) as OptionViewHolder
        optViewHolder.highlightOption(true)

        selectedOptId = optId
    }

    private fun highlightAns(pos: Int) {
        val question = questionsAdapter.currentList[pos]

        if (qid == question.qid) {
            val selPos = selectedOptId!![0] - 'A'
            val correctPos = question.correctAnswer[0] - 'A'

            val optionRV = binding.viewPager.findViewById<RecyclerView>(R.id.optionsRV)
            val selViewHolder = optionRV.findViewHolderForAdapterPosition(selPos) as OptionViewHolder

            if (selPos == correctPos) {
                selViewHolder.highlightAnswer(true)
                score++
            } else {
                val correctViewHolder = optionRV.findViewHolderForAdapterPosition(correctPos) as OptionViewHolder

                selViewHolder.highlightAnswer(false)
                correctViewHolder.highlightAnswer(true)
            }
        }
    }
}