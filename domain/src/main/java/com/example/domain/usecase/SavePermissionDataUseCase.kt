package com.example.domain.usecase

import androidx.compose.runtime.key
import com.example.domain.repository.PreferenceRepository
import javax.inject.Inject

class SavePermissionDataUseCase @Inject constructor(private val repository: PreferenceRepository) {
    suspend operator fun invoke(data: String){
        repository.saveString(key = "PERMISSION_DATA", data)
    }
}