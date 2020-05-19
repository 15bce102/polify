package com.example.polify.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.polify.R
import com.example.polify.databinding.LayoutAvatarBinding

class AvatarViewHolder(private val binding: LayoutAvatarBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): AvatarViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutAvatarBinding>(
                    inflater, R.layout.layout_avatar, parent, false
            )
            return AvatarViewHolder(binding)
        }
    }

    fun bind(avatarUrl: String) {
        binding.avatarUrl = avatarUrl
        binding.executePendingBindings()
    }
}