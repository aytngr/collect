package com.aytngr.domain.usecase

import com.aytngr.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetIsWidgetActiveUseCase @Inject constructor(private val repository: PreferenceRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.getBoolean(key = "IS_WIDGET_ACTIVE").first() ?: false
    }
}