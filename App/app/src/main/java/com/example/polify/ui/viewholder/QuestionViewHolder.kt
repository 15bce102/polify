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

        binding.optionsRV.apply {
            itemTouchListener = RecyclerTouchListener(context, this, object : RecyclerTouchListener.ClickListener {
                override fun onClick(view: View?, position: Int) {
                    onOptionClick(question, position, view!!)
                }

                override fun onLongClick(view: View?, position: Int) {}
            })

            adapter = OptionsAdapter().apply {
                submitList(question.options)
                setHasStableIds(true)
            }
            setHasFixedSize(true)
            addOnItemTouchListener(itemTouchListener)
        }
    }

    private fun onOptionClick(question: Question, position: Int, view: View) {
        val option = question.options[position]
        binding.optionsRV.removeOnItemTouchListener(itemTouchListener)
        EventBus.getDefault().post(OptionEvent(question.qid, option, view))
    }
}