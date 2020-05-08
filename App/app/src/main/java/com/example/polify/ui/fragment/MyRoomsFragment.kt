package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.polify.databinding.FragmentMyRoomsBinding
import com.example.polify.ui.adapter.RoomAdapter
import com.example.polify.ui.viewmodel.BaseViewModelFactory
import com.example.polify.ui.viewmodel.RoomViewModel
import com.google.firebase.auth.FirebaseAuth

class MyRoomsFragment : Fragment() {
    private val mAuth = FirebaseAuth.getInstance()

    private val roomAdapter = RoomAdapter()
    private val roomViewModel by viewModels<RoomViewModel> {
        BaseViewModelFactory { RoomViewModel(mAuth.currentUser?.uid ?: "") }
    }

    private lateinit var binding: FragmentMyRoomsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMyRoomsBinding.inflate(inflater, container, false)

        binding.recyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = roomAdapter
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        roomViewModel.roomLiveData.observe(viewLifecycleOwner, Observer { rooms ->
            roomAdapter.submitList(rooms)
        })
    }
}