package com.example.polify.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import com.andruid.magic.game.model.data.Option
import com.example.polify.R
import com.example.polify.databinding.LayoutOptionBinding

class OptionsAdapter(
        private val context: Context,
        private val options: List<Option>
) : BaseAdapter() {
    companion object {
        private val TAG = "${OptionsAdapter::class.java.simpleName}Log"
    }

    private var clicksEnabled = true

    override fun getCount() = options.size

    override fun getItem(position: Int) = options[position]

    override fun getItemId(position: Int) = (getItem(position).optId[0] - 'A').toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: LayoutOptionBinding

        Log.d(TAG, "getView: $position")

        if (convertView == null) {
            Log.d(TAG, "getView: convertView null")
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.layout_option, null)
            binding = DataBindingUtil.bind(view)!!
            view!!.tag = binding
        } else {
            Log.d(TAG, "getView: convertView not null")
            binding = convertView.tag as LayoutOptionBinding
        }

        binding.option = getItem(position)
        binding.executePendingBindings()

        Log.d(TAG, "getView: set binding data")
        return binding.root
    }

    override fun areAllItemsEnabled() = clicksEnabled

    override fun isEnabled(position: Int) = clicksEnabled

    fun disableClicks() {
        clicksEnabled = false
    }
}