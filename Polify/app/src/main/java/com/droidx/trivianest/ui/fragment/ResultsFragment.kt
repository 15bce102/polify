package com.droidx.trivianest.ui.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import com.droidx.trivianest.R
import com.droidx.trivianest.api.GameRepository
import com.droidx.trivianest.data.ACTION_MATCH_RESULTS
import com.droidx.trivianest.data.EXTRA_BATTLE_ID
import com.droidx.trivianest.data.EXTRA_PLAYERS
import com.droidx.trivianest.databinding.FragmentResultsBinding
import com.droidx.trivianest.model.data.PlayerResult
import com.droidx.trivianest.model.response.Result
import com.droidx.trivianest.ui.activity.HomeActivity
import com.droidx.trivianest.ui.adapter.ResultsAdapter
import com.droidx.trivianest.util.setOnSoundClickListener
import com.google.firebase.auth.FirebaseAuth
import com.muddzdev.styleabletoast.StyleableToast
import kotlinx.coroutines.launch

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

        if (battleId == "test")
            return

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

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentResultsBinding.inflate(inflater, container, false)

        if (battleId == "test") {
            binding.textView.visibility = View.VISIBLE
            binding.textScore.visibility = View.VISIBLE
            binding.textScore.text = "Your Score : $score"

            binding.textView.setOnSoundClickListener {
                val intent = Intent(context, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        } else {
            binding.loadingView.visibility = View.VISIBLE
        }

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
        resultsAdapter.submitList(results.sortedByDescending { result->result.player.score }) {
            binding.loadingView.visibility = View.GONE

            val user = mAuth.currentUser ?: return@submitList

            val player = results.find { playerResult -> playerResult.player.uid == user.uid }!!

            val msg = getString(if (player.coinsUpdate[0] == '+')
                R.string.message_win
            else
                R.string.message_lose, player.coinsUpdate.substring(1))

            StyleableToast.makeText(requireContext(), msg, Toast.LENGTH_LONG, R.style.mtToast).show()
        }
    }
}