package com.droidx.trivianest.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.droidx.gameapi.model.data.Option
import com.droidx.trivianest.ui.viewholder.OptionViewHolder

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Option>() {
    override fun areItemsTheSame(oldItem: Option, newItem: Option) =
            oldItem.optId == newItem.optId

    override fun areContentsTheSame(oldItem: Option, newItem: Option) =
            oldItem == newItem
}

class OptionsAdapter : ListAdapter<Option, OptionViewHolder>(DIFF_CALLBACK) {
    override fun getItemId(position: Int) = (getItem(position).optId[0] - 'A').toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            OptionViewHolder.from(parent)

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}