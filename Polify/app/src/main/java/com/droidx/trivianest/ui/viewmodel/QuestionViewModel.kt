package com.droidx.trivianest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.droidx.gameapi.api.GameRepository
import com.droidx.trivianest.data.BATTLE_TEST
import com.droidx.gameapi.model.data.Battle
import com.droidx.gameapi.model.response.QuestionsResponse
import com.droidx.gameapi.model.response.Result

class QuestionViewModel(battle: Battle?, battleType: String) : ViewModel() {
    val questions = liveData {
        emit(Result.loading(null))

        if (battleType == BATTLE_TEST) {
            val questions = GameRepository.getPracticeQuestions()
            emit(Result.success(QuestionsResponse(true, null, questions)))
        } else {
            val response = GameRepository.getBattleQuestions(battle?.battleId ?: "")
            emit(response)
        }
    }
}