package com.droidx.gameapi.model.data

import kotlinx.android.parcel.Parcelize

@Parcelize
data class TestBattle(
    override val players: List<Player>
) : Battle("battle_test", System.currentTimeMillis(), players, 0)