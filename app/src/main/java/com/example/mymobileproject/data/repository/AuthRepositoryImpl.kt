package com.example.mymobileproject.data.repository

import com.example.mymobileproject.domain.model.User
import com.example.mymobileproject.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val fbUser = firebaseAuth.currentUser
            trySend(fbUser?.let {
                User(
                    uid = it.uid,
                    displayName = it.displayName ?: "",
                    email = it.email ?: "",
                    photoUrl = it.photoUrl?.toString()
                )
            })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val fbUser = result.user ?: throw Exception("Sign in failed")

        val user = User(
            uid = fbUser.uid,
            displayName = fbUser.displayName ?: "",
            email = fbUser.email ?: "",
            photoUrl = fbUser.photoUrl?.toString()
        )

        // Save user profile to Firestore
        firestore.collection("users").document(user.uid).set(
            mapOf(
                "displayName" to user.displayName,
                "email" to user.email,
                "photoUrl" to user.photoUrl
            )
        ).await()

        user
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun isLoggedIn(): Boolean = auth.currentUser != null
}
