package com.aytngr.domain.storage

interface ImageStore {
    suspend fun delete(paths: List<String>)
}