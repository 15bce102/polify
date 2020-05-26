package com.droidx.trivianest.ui.viewholder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.droidx.trivianest.R
import com.droidx.trivianest.databinding.LayoutOptionBinding
import com.droidx.trivianest.model.data.Option
import splitties.resources.color

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
        val bg = if (correct) binding.root.context.color(R.color.colorCorrect) else Color.RED
        binding.optionCard.setCardBackgroundColor(bg)
    }

    fun highlightOption() {
        binding.optionCard.setCardBackgroundColor(Color.LTGRAY)
    }
}