package com.example.polify.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.game.model.data.Option
import com.example.polify.data.VIEW_TYPE_OPTIONS
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

    override fun getItemId(position: Int) = (options[position].optId[0]-'A').toLong()

    override fun getItemViewType(position: Int) = VIEW_TYPE_OPTIONS
}