package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.andruid.magic.game.model.response.Result
import com.example.polify.databinding.FragmentRoomBinding
import com.example.polify.ui.adapter.FriendAdapter
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.FriendViewModel
import com.google.firebase.auth.FirebaseAuth

class RoomFragment : Fragment() {
    private lateinit var binding: FragmentRoomBinding

    private val friendAdapter = FriendAdapter()
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val friendViewModel by viewModels<FriendViewModel> {
        BaseViewModelFactory { FriendViewModel(mAuth.currentUser?.uid ?: "") }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentRoomBinding.inflate(inflater, container, false)

        initRecyclerView()
        friendViewModel.friends.observe(viewLifecycleOwner, Observer { result ->
            if (result.status == Result.Status.SUCCESS)
                friendAdapter.submitList(result.data?.friends)
        })

        return binding.root
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = friendAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }
}