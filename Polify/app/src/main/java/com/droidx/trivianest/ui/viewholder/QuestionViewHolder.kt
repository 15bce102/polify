package com.droidx.trivianest.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.droidx.trivianest.R
import com.droidx.trivianest.databinding.LayoutQuestionBinding
import com.droidx.trivianest.eventbus.OptionEvent
import com.droidx.trivianest.model.data.Question
import com.droidx.trivianest.ui.adapter.OptionsAdapter
import com.droidx.trivianest.util.RecyclerTouchListener
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

        binding.optionsRV.apply {
            itemTouchListener = RecyclerTouchListener(context, this, object : RecyclerTouchListener.ClickListener {
                override fun onClick(view: View?, position: Int) {
                    super.onClick(view, position)
                    onOptionClick(question, position, view!!)
                }
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