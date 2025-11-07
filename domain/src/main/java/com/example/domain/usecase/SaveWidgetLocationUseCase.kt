package com.example.domain.usecase

import androidx.compose.runtime.key
import com.example.domain.repository.PreferenceRepository
import javax.inject.Inject

class SaveWidgetLocationUseCase @Inject constructor(private val repository: PreferenceRepository) {
    suspend operator fun invoke(x: Int, y: Int, isRightSide: Boolean){
        repository.saveInt(key = "WIDGET_X", x)
        repository.saveInt(key = "WIDGET_Y", y)
        repository.saveBoolean(key = "IS_RIGHT_SIDE", isRightSide)
    }
}