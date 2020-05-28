package com.droidx.trivianest.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.droidx.trivianest.R
import com.droidx.trivianest.databinding.LayoutResultBinding
import com.droidx.gameapi.model.data.PlayerResult

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