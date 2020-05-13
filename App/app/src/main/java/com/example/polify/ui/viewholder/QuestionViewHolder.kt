package com.example.polify.ui.viewholder

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.game.model.data.Question
import com.example.polify.R
import com.example.polify.databinding.LayoutQuestionBinding
import com.example.polify.eventbus.OptionEvent
import com.example.polify.ui.adapter.OptionsAdapter
import org.greenrobot.eventbus.EventBus

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

    fun bind(question: Question, pos: Int) {
        binding.question = question
        binding.position = pos

        binding.textMainQuestion.setOnClickListener {
            Log.d("optionLog", "question text click")
        }

        binding.optionsLV.apply {
            adapter = OptionsAdapter(context, question.options)
            setOnItemClickListener { _, view, position, _ ->
                Log.d("OptionsAdapterLog", "list view item click for $position")
                onOptionClick(question, position, view)
            }
        }
    }

    private fun onOptionClick(question: Question, position: Int, view: View) {
        val option = question.options[position]
        (binding.optionsLV.adapter as OptionsAdapter).disableClicks()
        EventBus.getDefault().post(OptionEvent(question.qid, option, view))
    }
}