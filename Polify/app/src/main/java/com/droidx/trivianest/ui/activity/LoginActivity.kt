package com.droidx.trivianest.ui.activity

import android.os.Bundle
import androidx.navigation.findNavController
import com.droidx.trivianest.R
import com.droidx.trivianest.util.isFirstTime
import com.droidx.trivianest.util.setFirstTimeComplete

class LoginActivity : FullScreenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val navGraph =
                if (isFirstTime())
                    R.navigation.navigation_welcome
                else
                    R.navigation.navigation_login
        findNavController(R.id.nav_host_fragment).setGraph(navGraph)

        setFirstTimeComplete()
    }
}