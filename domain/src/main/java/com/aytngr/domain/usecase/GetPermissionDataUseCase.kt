package com.aytngr.domain.usecase

import com.aytngr.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPermissionDataUseCase @Inject constructor(private val repository: PreferenceRepository) {
    suspend operator fun invoke(): String?{
       return repository.getString(key = "PERMISSION_DATA").first()
    }
}