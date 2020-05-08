package com.example.polify.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.andruid.magic.game.api.GameRepository
import com.example.polify.R
import com.example.polify.databinding.ActivityMultiPlayerBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MultiPlayerActivity : AppCompatActivity() {
    private val mAuth = FirebaseAuth.getInstance()

    private lateinit var binding: ActivityMultiPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createBtn.setOnClickListener {
            mAuth.currentUser?.let { user ->
                GlobalScope.launch(Dispatchers.IO) {
                    GameRepository.createBattle(user.uid, 10)?.let { resp ->
                        if (resp.success) {
                            runOnUiThread {
                                Toast.makeText(this@MultiPlayerActivity, "Battle created: ${resp.battle?.battleId ?: "null"}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@MultiPlayerActivity, resp.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } ?: run {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }

        binding.joinBtn.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.layout_join_battle, binding.rootViewGroup,
                    false)

            val builder = AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton("Join") { dialog, _ ->
                        val bid = view.findViewById<EditText>(R.id.battleIdET).text.toString().trim()
                        dialog.dismiss()

                        mAuth.currentUser?.let { user ->
                            GlobalScope.launch(Dispatchers.IO) {
                                GameRepository.joinBattle(user.uid, bid)?.let { resp ->
                                    if (resp.success) {
                                        runOnUiThread {
                                            Toast.makeText(this@MultiPlayerActivity, "Joined battle", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
            builder.create().show()
        }
    }
}