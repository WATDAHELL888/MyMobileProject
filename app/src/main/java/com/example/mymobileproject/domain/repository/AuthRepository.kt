package com.example.mymobileproject.domain.repository

import com.example.mymobileproject.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
    fun isLoggedIn(): Boolean
}
