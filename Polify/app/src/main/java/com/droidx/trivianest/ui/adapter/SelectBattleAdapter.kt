package com.droidx.trivianest.ui.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.droidx.trivianest.model.BattleSelect
import com.droidx.trivianest.ui.fragment.SelectBattleFragment

class SelectBattleAdapter(
        activity: AppCompatActivity,
        private val battles: List<BattleSelect>) : FragmentStateAdapter(activity) {
    override fun getItemCount() = battles.size

    override fun createFragment(position: Int) = SelectBattleFragment.getInstance(battles[position])
}