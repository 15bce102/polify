package com.example.polify.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.polify.databinding.FragmentNewGameBinding
import com.example.polify.ui.activity.MultiPlayerActivity

class NewGameFragment : Fragment() {
    private lateinit var binding: FragmentNewGameBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentNewGameBinding.inflate(inflater, container, false)

        binding.multiPlayerBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), MultiPlayerActivity::class.java))
        }

        return binding.root
    }
}