package com.example.polify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.andruid.magic.game.api.GameRepository
import com.example.polify.data.BATTLE_TEST

class QuestionViewModel(bid: String, battleType: String) : ViewModel() {
    val questions = liveData {
        if (battleType == BATTLE_TEST) {
            val questions = GameRepository.getPracticeQuestions()
            emit(questions)
        } else {
            val response = GameRepository.getBattleQuestions(bid)
            if (response?.success == true)
                emit(response.questions)
        }
    }
}