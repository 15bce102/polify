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
import com.example.polify.data.VIEW_TYPE_OPTIONS
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

    private lateinit var itemTouchListener: RecyclerTouchListener

    fun bind(question: Question, pos: Int) {
        binding.question = question
        binding.position = pos

        binding.textMainQuestion.setOnClickListener {
            Log.d("optionLog", "question text click")
        }

        itemTouchListener = RecyclerTouchListener(binding.root.context, binding.optionsRV,
                object : RecyclerTouchListener.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        onOptionClick(question, position)
                    }

                    override fun onLongClick(view: View?, position: Int) {}
                })

        binding.optionsRV.apply {
            adapter = OptionsAdapter(question.options).apply {
                setHasFixedSize(true)
            }
            recycledViewPool.setMaxRecycledViews(VIEW_TYPE_OPTIONS, 0)
            itemAnimator = DefaultItemAnimator()
            addOnItemTouchListener(itemTouchListener)
        }
    }

    private fun onOptionClick(question: Question, position: Int) {
        val option = question.options[position]
        EventBus.getDefault().post(OptionEvent(question.qid, option))
        binding.optionsRV.removeOnItemTouchListener(itemTouchListener)
    }
}