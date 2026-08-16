package com.aytngr.feature.overlay.di

import com.aytngr.domain.overlay.OverlayController
import com.aytngr.feature.overlay.OverlayControllerImpl
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
