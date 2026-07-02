package com.example.feature.overlay.di

import com.example.domain.overlay.OverlayController
import com.example.feature.overlay.OverlayControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OverlayModule {
    @Binds
    abstract fun bindOverlayController(impl: OverlayControllerImpl): OverlayController
}
