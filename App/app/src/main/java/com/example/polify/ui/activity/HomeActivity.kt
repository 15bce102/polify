package com.example.polify.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.User
import com.example.polify.R
import com.example.polify.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private val mAuth = FirebaseAuth.getInstance()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.nav_new_game, R.id.nav_my_rooms, R.id.nav_history
                ), binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        mAuth.currentUser?.let { user ->
            GlobalScope.launch(Dispatchers.Main) {
                GameRepository.login(user.uid)?.let { resp ->
                    if (resp.success) {
                        currentUser = resp.user!!
                        Toast.makeText(this@HomeActivity, "${currentUser.coins} coins available", Toast.LENGTH_LONG).show()
                    }
                    else
                        Toast.makeText(this@HomeActivity, resp.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}