package com.droidx.trivianest.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.droidx.trivianest.R
import com.droidx.trivianest.data.EXTRA_PLAYERS
import com.droidx.gameapi.model.data.Player
import com.droidx.gameapi.model.data.TestBattle

class PracticeActivity : FullScreenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val players: List<Player> = intent.extras?.getParcelableArrayList(EXTRA_PLAYERS) ?: arrayListOf()

        val navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.navigation_practice, bundleOf(
                "battle" to TestBattle(players)
        ))
    }
}