package com.aytngr.domain.usecase

import com.aytngr.domain.repository.PreferenceRepository
import javax.inject.Inject

class SavePermissionDataUseCase @Inject constructor(private val repository: PreferenceRepository) {
    suspend operator fun invoke(data: String){
        repository.saveString(key = "PERMISSION_DATA", data)
    }
}