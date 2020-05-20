package com.example.polify.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.polify.databinding.FragmentResultsMultiplayerBinding

class ResultsMultiplayerFragment : Fragment() {
    private lateinit var binding: FragmentResultsMultiplayerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentResultsMultiplayerBinding.inflate(inflater, container, false)
        return binding.root
    }
}