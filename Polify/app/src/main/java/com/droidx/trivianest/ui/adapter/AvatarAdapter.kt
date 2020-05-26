package com.droidx.trivianest.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.droidx.trivianest.ui.viewholder.AvatarViewHolder

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String) =
            oldItem == newItem

    override fun areContentsTheSame(oldItem: String, newItem: String) =
            oldItem == newItem
}

class AvatarAdapter : ListAdapter<String, AvatarViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AvatarViewHolder.from(parent)

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}