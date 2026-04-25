package com.example.mymobileproject.core.di

import com.example.mymobileproject.data.repository.AIRepositoryImpl
import com.example.mymobileproject.data.repository.AuthRepositoryImpl
import com.example.mymobileproject.data.repository.GroupRepositoryImpl
import com.example.mymobileproject.data.repository.TransactionRepositoryImpl
import com.example.mymobileproject.domain.repository.AIRepository
import com.example.mymobileproject.domain.repository.AuthRepository
import com.example.mymobileproject.domain.repository.GroupRepository
import com.example.mymobileproject.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds @Singleton
    abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository

    @Binds @Singleton
    abstract fun bindAIRepository(impl: AIRepositoryImpl): AIRepository
}
