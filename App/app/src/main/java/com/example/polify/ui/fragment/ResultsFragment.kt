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
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.PlayerResult
import com.example.polify.R
import com.example.polify.data.*
import com.example.polify.databinding.FragmentResultsOneVsOneBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ResultsFragment : Fragment() {
    companion object {
        private val TAG = "${ResultsFragment::class.java.simpleName}Log"
    }

    private lateinit var binding: ViewDataBinding
    private lateinit var battleId: String

    private var battleType = BATTLE_TEST
    private var score = 0

    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val resultsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_MATCH_RESULTS) {
                intent.extras?.let { extras ->
                    val battleId = extras.getString(EXTRA_BATTLE_ID)
                    if (battleId != this@ResultsFragment.battleId)
                        return

                    val results: ArrayList<PlayerResult> = extras.getParcelableArrayList(EXTRA_PLAYERS)
                            ?: arrayListOf()

                    Log.d(TAG, "battle score received: size = ${results.size}")

                    when (battleType) {
                        BATTLE_ONE_VS_ONE -> showOneVsOneResults(results)
                    }
                }
            }
        }
    }

    private fun showOneVsOneResults(results: List<PlayerResult>) {
        Log.d(TAG, "battle showing results  = ${results.size}")
        if (results.size != 2)
            return

        (binding as FragmentResultsOneVsOneBinding).apply {
            mAuth.currentUser?.let {
                val user: PlayerResult
                val opponent: PlayerResult

                if (results[0].player.uid == it.uid) {
                    user = results[0]
                    opponent = results[1]
                } else {
                    user = results[1]
                    opponent = results[0]
                }

                val resultMsg = when {
                    user.player.score > opponent.player.score -> R.string.message_win
                    user.player.score < opponent.player.score -> R.string.message_lose
                    else -> R.string.message_tie
                }

                message = getString(resultMsg)
                this.user = user
                this.opponent = opponent
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val (battleId, battleType, score) = ResultsFragmentArgs.fromBundle(it)
            this.battleId = battleId
            this.battleType = battleType
            this.score = score

            Log.d(TAG, "battle type = $battleType, 1v1 = $BATTLE_ONE_VS_ONE")

            if (battleType != BATTLE_TEST) {
                LocalBroadcastManager.getInstance(requireContext())
                        .registerReceiver(resultsReceiver, IntentFilter(ACTION_MATCH_RESULTS))

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        when (battleType) {
            BATTLE_TEST -> {
                binding = DataBindingUtil.inflate(inflater, R.layout.fragment_results_test,
                        container, false)
            }
            BATTLE_ONE_VS_ONE -> {
                binding = DataBindingUtil.inflate(inflater, R.layout.fragment_results_one_vs_one,
                        container, false)
            }
            BATTLE_MULTIPLAYER -> {
                binding = DataBindingUtil.inflate(inflater, R.layout.fragment_results_multiplayer,
                        container, false)
            }
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        if (battleType != BATTLE_TEST)
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(resultsReceiver)
    }
}