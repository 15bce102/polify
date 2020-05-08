package com.example.polify.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.game.model.Battle
import com.example.polify.R
import com.example.polify.databinding.LayoutMyRoomsBinding

class RoomViewHolder(private val binding: LayoutMyRoomsBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): RoomViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutMyRoomsBinding>(
                    inflater, R.layout.layout_my_rooms, parent, false
            )
            return RoomViewHolder(binding)
        }
    }

    fun bind(room: Battle) {
        binding.room = room
    }
}