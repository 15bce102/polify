package com.example.polify.ui.viewholder

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.game.model.data.Question
import com.example.polify.R
import com.example.polify.databinding.LayoutQuestionBinding
import com.example.polify.eventbus.OptionEvent
import com.example.polify.ui.adapter.OptionsAdapter
import com.example.polify.util.RecyclerTouchListener
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

    fun bind(question: Question, position: Int) {
        binding.question = question
        binding.position = position

        binding.textMainQuestion.setOnClickListener {
            Log.d("optionLog", "question text click")
        }

        binding.optionsRV.apply {
            adapter = OptionsAdapter(question.options).apply {
                setHasFixedSize(true)
            }
            itemAnimator = DefaultItemAnimator()
            addOnItemTouchListener(RecyclerTouchListener(rootView.context, this, object : RecyclerTouchListener.ClickListener {
                override fun onClick(view: View?, position: Int) {
                    Log.d("optionLog", "on option button click")

                    val option = question.options[position]
                    EventBus.getDefault().post(OptionEvent(question.qid, option))
                }

                override fun onLongClick(view: View?, position: Int) {}
            }))
        }
    }
}