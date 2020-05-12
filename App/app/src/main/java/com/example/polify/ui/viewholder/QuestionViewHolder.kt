package com.example.polify.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.game.model.data.Question
import com.example.polify.R
import com.example.polify.databinding.LayoutQuestionBinding
import com.example.polify.ui.adapter.OptionsAdapter

class QuestionViewHolder(private val binding: LayoutQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): QuestionViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutQuestionBinding>(
                    inflater, R.layout.layout_question, parent, false
            )
            return QuestionViewHolder(binding)
        }
    }

    fun bind(question: Question, position: Int) {
        binding.question = question
        binding.position = position

        binding.optionsRV.apply {
            adapter = OptionsAdapter(question.options)
            itemAnimator = DefaultItemAnimator()
        }
    }
}