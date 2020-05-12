package com.example.polify.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.game.model.data.Option
import com.example.polify.ui.viewholder.OptionViewHolder

class OptionsAdapter(
        private val options: List<Option>
) : RecyclerView.Adapter<OptionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            OptionViewHolder.from(parent)

    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val option = options[position]
        holder.bind(option)
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position
}