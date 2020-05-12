package com.example.polify.ui.viewholder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.game.model.data.Option
import com.example.polify.R
import com.example.polify.databinding.LayoutOptionBinding

class OptionViewHolder(private val binding: LayoutOptionBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): OptionViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutOptionBinding>(
                    inflater, R.layout.layout_option, parent, false
            )
            return OptionViewHolder(binding)
        }
    }

    fun bind(option: Option) {
        binding.option = option
    }

    fun highlightAnswer(correct: Boolean) {
        val bg = if (correct) Color.GREEN else Color.RED
        binding.linearLayout.setBackgroundColor(bg)
    }

    fun highlightOption(highlight: Boolean) {
        if (highlight)
            binding.linearLayout.setBackgroundColor(Color.LTGRAY)
        else
            binding.linearLayout.setBackgroundResource(R.drawable.layout)
    }
}