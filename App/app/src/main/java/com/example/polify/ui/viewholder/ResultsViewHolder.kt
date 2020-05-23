package com.example.polify.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.game.model.data.PlayerResult
import com.example.polify.R
import com.example.polify.databinding.LayoutResultBinding

class ResultsViewHolder(private val binding: LayoutResultBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): ResultsViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutResultBinding>(
                    inflater, R.layout.layout_result, parent, false
            )
            return ResultsViewHolder(binding)
        }
    }

    fun bind(playerResult: PlayerResult) {
        binding.apply {
            result = playerResult
            executePendingBindings()
        }
    }
}