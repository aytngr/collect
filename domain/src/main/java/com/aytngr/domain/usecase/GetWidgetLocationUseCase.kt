package com.aytngr.domain.usecase

import com.aytngr.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetWidgetLocationUseCase @Inject constructor(private val repository: PreferenceRepository) {
    suspend operator fun invoke(): Triple<Int?, Int?, Boolean?> {
        return Triple(
            repository.getInt(key = "WIDGET_X").first(),
            repository.getInt(key = "WIDGET_Y").first(),
            repository.getBoolean(key = "IS_RIGHT_SIDE").first() ?: true
        )
    }
}