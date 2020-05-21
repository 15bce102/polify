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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.PlayerResult
import com.andruid.magic.game.model.response.Result
import com.example.polify.data.ACTION_MATCH_RESULTS
import com.example.polify.data.EXTRA_BATTLE_ID
import com.example.polify.data.EXTRA_PLAYERS
import com.example.polify.databinding.FragmentResultsBinding
import com.example.polify.ui.adapter.ResultsAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import splitties.toast.longToast

class ResultsFragment : Fragment() {
    companion object {
        private val TAG = "${this::class.java.simpleName}Log"
    }

    private val args by navArgs<ResultsFragmentArgs>()
    private val battleId by lazy { args.battleId }
    private val score by lazy { args.score }

    private val resultsAdapter = ResultsAdapter()
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val resultsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_MATCH_RESULTS) {
                intent.extras?.let { extras ->
                    val battleId = extras.getString(EXTRA_BATTLE_ID)
                    if (battleId != this@ResultsFragment.battleId)
                        return

                    val results: ArrayList<PlayerResult> =
                            extras.getParcelableArrayList(EXTRA_PLAYERS) ?: arrayListOf()

                    Log.d(TAG, "battle score received: size = ${results.size}")
                    showResults(results)
                }
            }
        }
    }

    private lateinit var binding: FragmentResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (battleId == "test") {
            longToast("Your score = $score")
            return
        }

        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(resultsReceiver, IntentFilter(ACTION_MATCH_RESULTS))

        val user = mAuth.currentUser ?: return

        lifecycleScope.launch {
            val response = GameRepository.updateBattleScore(battleId, user.uid, score)
            if (response.status == Result.Status.SUCCESS)
                Log.d(TAG, "score updated")
            else
                Log.d(TAG, "score not updated")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentResultsBinding.inflate(inflater, container, false)

        initRecyclerView()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(resultsReceiver)
    }

    private fun initRecyclerView() {
        binding.resultsRV.apply {
            adapter = resultsAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun showResults(results: List<PlayerResult>) {
        resultsAdapter.submitList(results)
    }
}