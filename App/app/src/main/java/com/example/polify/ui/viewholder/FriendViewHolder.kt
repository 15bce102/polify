package com.example.polify.ui.viewholder

import android.graphics.Color
import android.os.Handler

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.game.model.data.Friend
import com.example.polify.R
import com.example.polify.databinding.LayoutFriendBinding
import com.example.polify.eventbus.FriendInviteEvent
import com.example.polify.util.setOnSoundClickListener
import com.muddzdev.styleabletoast.StyleableToast
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.concurrent.schedule


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


