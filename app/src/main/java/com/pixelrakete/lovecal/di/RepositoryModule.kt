package com.pixelrakete.lovecal.di

import com.pixelrakete.lovecal.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDatePlanRepository(
        datePlanRepositoryImpl: DatePlanRepositoryImpl
    ): DatePlanRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        calendarRepositoryImpl: CalendarRepositoryImpl
    ): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindQuoteRepository(
        quoteRepositoryImpl: QuoteRepositoryImpl
    ): QuoteRepository

    @Binds
    @Singleton
    abstract fun bindCoupleRepository(
        coupleRepositoryImpl: CoupleRepositoryImpl
    ): CoupleRepository

    @Binds
    @Singleton
    abstract fun bindDateWishRepository(
        dateWishRepositoryImpl: DateWishRepositoryImpl
    ): DateWishRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindCoupleSettingsRepository(
        coupleSettingsRepositoryImpl: CoupleSettingsRepositoryImpl
    ): CoupleSettingsRepository
} 