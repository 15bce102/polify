package com.droidx.trivianest.ui.viewholder

import android.os.Handler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.droidx.trivianest.R
import com.droidx.trivianest.databinding.LayoutFriendBinding
import com.droidx.trivianest.eventbus.FriendInviteEvent
import com.droidx.trivianest.model.data.Friend
import com.droidx.trivianest.util.setOnSoundClickListener
import org.greenrobot.eventbus.EventBus


class FriendViewHolder(private val binding: LayoutFriendBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): FriendViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutFriendBinding>(
                    inflater, R.layout.layout_friend, parent, false
            )
            return FriendViewHolder(binding)
        }
    }

    fun bind(friend: Friend) {
        binding.friend = friend

        binding.sendRequest.setOnSoundClickListener {
            EventBus.getDefault().post(FriendInviteEvent(friend))
        }

        binding.executePendingBindings()
    }

    fun hide(){
        binding.sendRequest.isEnabled = false
        binding.sendRequest.setImageResource(R.drawable.tick)
        Handler().postDelayed({
            binding.sendRequest.isEnabled = true
            binding.sendRequest.setImageResource(R.drawable.plus)
        }, 10000)
    }
}


