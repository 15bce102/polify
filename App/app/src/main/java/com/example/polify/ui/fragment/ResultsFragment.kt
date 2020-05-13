package com.example.polify.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.Player
import com.example.polify.data.ACTION_MATCH_RESULTS
import com.example.polify.data.EXTRA_PLAYERS
import com.example.polify.databinding.FragmentResultsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ResultsFragment : Fragment() {
    companion object {
        private val TAG = "${ResultsFragment::class.java.simpleName}Log"
    }

    private val mAuth by lazy { FirebaseAuth.getInstance() }

    private lateinit var binding: FragmentResultsBinding
    private lateinit var battleId: String

    private val resultsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_MATCH_RESULTS) {
                val players: ArrayList<Player> = intent.extras?.getParcelableArrayList(EXTRA_PLAYERS)
                        ?: arrayListOf()
                binding.textView.text = players.joinToString(", ", "[", "]")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(resultsReceiver, IntentFilter(ACTION_MATCH_RESULTS))

        arguments?.let {
            val (battleId, score) = ResultsFragmentArgs.fromBundle(it)
            this.battleId = battleId

            lifecycleScope.launch {
                mAuth.currentUser?.let { user ->
                    val response = GameRepository.updateBattleScore(battleId, user.uid, score)
                    if (response?.success == true)
                        Log.d(TAG, "score updated")
                    else
                        Log.d(TAG, "score not updated")
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(resultsReceiver)
    }
}