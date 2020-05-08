package com.example.polify.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.game.model.Battle
import com.example.polify.ui.viewholder.RoomViewHolder

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Battle>() {
    override fun areItemsTheSame(oldItem: Battle, newItem: Battle) =
            oldItem.battleId == newItem.battleId

    override fun areContentsTheSame(oldItem: Battle, newItem: Battle) =
            oldItem == newItem
}

class RoomAdapter : PagedListAdapter<Battle, RoomViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RoomViewHolder.from(parent)

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        getItem(position)?.let { room ->
            holder.bind(room)
        }
    }
}