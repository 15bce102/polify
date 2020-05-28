package com.droidx.trivianest.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.droidx.gameapi.model.data.Question
import com.droidx.trivianest.ui.viewholder.QuestionViewHolder

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Question>() {

    override fun areItemsTheSame(oldItem: Question, newItem: Question) =
            oldItem.qid == newItem.qid

    override fun areContentsTheSame(oldItem: Question, newItem: Question) =
            oldItem == newItem
}

class QuestionAdapter : ListAdapter<Question, QuestionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            QuestionViewHolder.from(parent)

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = getItem(position)
        holder.bind(question, position+1)
    }
}