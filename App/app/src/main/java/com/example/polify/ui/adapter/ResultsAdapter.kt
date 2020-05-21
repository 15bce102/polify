package com.example.polify.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.andruid.magic.game.model.data.PlayerResult
import com.example.polify.ui.viewholder.ResultsViewHolder

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PlayerResult>() {
    override fun areItemsTheSame(oldItem: PlayerResult, newItem: PlayerResult) =
            oldItem.player.uid == newItem.player.uid

    override fun areContentsTheSame(oldItem: PlayerResult, newItem: PlayerResult) =
            oldItem == newItem
}

class ResultsAdapter : ListAdapter<PlayerResult, ResultsViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ResultsViewHolder.from(parent)

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}