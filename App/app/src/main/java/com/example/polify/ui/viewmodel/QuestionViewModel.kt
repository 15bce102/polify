package com.example.polify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.andruid.magic.game.api.GameRepository

class QuestionViewModel(bid: String) : ViewModel() {
    val questions = liveData {
        val response = GameRepository.getBattleQuestions(bid)
        if (response?.success == true)
            emit(response.questions)
    }
}