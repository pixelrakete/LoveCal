package com.pixelrakete.lovecal.di

import com.google.gson.Gson
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.services.ai.GeminiService
import com.pixelrakete.lovecal.data.services.ai.GeminiWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    @Provides
    @Singleton
    fun provideGeminiService(
        gson: Gson,
        userManager: UserManager,
        geminiWrapper: GeminiWrapper
    ): GeminiService {
        return GeminiService(gson, userManager, geminiWrapper)
    }
} 