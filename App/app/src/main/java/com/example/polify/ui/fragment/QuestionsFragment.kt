package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.example.polify.databinding.FragmentQuestionsBinding
import com.example.polify.ui.adapter.QuestionAdapter
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.QuestionViewModel

class QuestionsFragment : Fragment() {
    private val questionsAdapter = QuestionAdapter()
    private val questionsViewModel by viewModels<QuestionViewModel> {
        BaseViewModelFactory {
            QuestionViewModel(battleId)
        }
    }

    private lateinit var battleId: String
    private lateinit var binding: FragmentQuestionsBinding

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
                }
            })
        }

        binding.nextBtn.setOnClickListener {
            val pos = binding.viewPager.currentItem
            if (pos < questionsAdapter.itemCount)
                binding.viewPager.setCurrentItem(pos + 1, true)
            else
                Toast.makeText(requireContext(), "Finished!", Toast.LENGTH_SHORT).show()
        }

        questionsViewModel.questions.observe(viewLifecycleOwner, Observer {
            questionsAdapter.submitList(it)
            binding.barProgressBar.max = it.size
        })

        return binding.root
    }
}