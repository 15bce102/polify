package com.droidx.trivianest.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.droidx.trivianest.model.data.Friend
import com.droidx.trivianest.ui.viewholder.FriendViewHolder

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Friend>() {
    override fun areItemsTheSame(oldItem: Friend, newItem: Friend) =
            oldItem.uid == newItem.uid

    override fun areContentsTheSame(oldItem: Friend, newItem: Friend) =
            oldItem == newItem
}

class FriendAdapter : ListAdapter<Friend, FriendViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            FriendViewHolder.from(parent)

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}