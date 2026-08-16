package com.aytngr.domain.usecase

import com.aytngr.domain.repository.PreferenceRepository
import javax.inject.Inject

class SaveIsWidgetActiveUseCase @Inject constructor(private val repository: PreferenceRepository) {
    suspend operator fun invoke(isActive: Boolean){
        repository.saveBoolean(key = "IS_WIDGET_ACTIVE", isActive)
    }
}