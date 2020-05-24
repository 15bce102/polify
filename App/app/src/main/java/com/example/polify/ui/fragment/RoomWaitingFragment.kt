package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.polify.R
import com.example.polify.databinding.FragmentRoomWaitingBinding

class RoomWaitingFragment : Fragment() {
    private lateinit var binding: FragmentRoomWaitingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentRoomWaitingBinding.inflate(inflater, container, false)

        binding.loadingAnimView.setAnimation(R.raw.loading)
        binding.loadingView.visibility = View.VISIBLE
        binding.loadingAnimView.playAnimation()
        return binding.root
    }
}