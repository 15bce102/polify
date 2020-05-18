package com.example.polify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.andruid.magic.game.api.GameRepository
import com.andruid.magic.game.model.data.Battle
import com.andruid.magic.game.model.response.QuestionsResponse
import com.example.polify.data.BATTLE_TEST
import com.andruid.magic.game.model.response.Result


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