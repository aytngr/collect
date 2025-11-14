package com.example.domain.usecase

import androidx.compose.runtime.key
import com.example.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPermissionDataUseCase @Inject constructor(private val repository: PreferenceRepository) {
    suspend operator fun invoke(): String?{
       return repository.getString(key = "PERMISSION_DATA").first()
    }
}