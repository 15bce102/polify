package com.example.polify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.andruid.magic.game.api.GameRepository

class QuestionViewModel(bid: String, offline: Boolean = false) : ViewModel() {
    val questions = liveData {
        if (offline) {
            val questions = GameRepository.getPracticeQuestions()
            emit(questions)
        } else {
            val response = GameRepository.getBattleQuestions(bid)
            if (response?.success == true)
                emit(response.questions)
        }
    }
}