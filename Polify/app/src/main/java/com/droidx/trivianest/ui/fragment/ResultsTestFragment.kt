package com.droidx.trivianest.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.droidx.trivianest.databinding.FragmentResultsTestBinding

class ResultsTestFragment : Fragment() {
    private lateinit var binding: FragmentResultsTestBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentResultsTestBinding.inflate(inflater, container, false)

        return binding.root
    }
}