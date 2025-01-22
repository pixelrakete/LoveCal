package com.pixelrakete.lovecal.data.repository

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface AuthRepository {
    suspend fun signInWithGoogle(account: GoogleSignInAccount): FirebaseUser?
    suspend fun signOut()
    fun getCurrentUser(): FirebaseUser?
}

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun signInWithGoogle(account: GoogleSignInAccount): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        return try {
            Log.d("AuthRepository", "Starting Firebase auth with Google credential for: ${account.email}")
            
            // Check if we're already signed in with this account
            val currentUser = auth.currentUser
            if (currentUser?.email == account.email) {
                Log.d("AuthRepository", "Already signed in with this account: ${account.email}")
                return currentUser
            }
            
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                Log.d("AuthRepository", "Firebase auth successful: ${user.email}")
                // Get fresh ID token to ensure it's valid
                user.getIdToken(true).await()
                Log.d("AuthRepository", "ID token refreshed for: ${user.email}")
            } else {
                Log.e("AuthRepository", "Firebase auth failed: user is null")
            }
            user
        } catch (e: Exception) {
            Log.e("AuthRepository", "Firebase auth failed with exception", e)
            null
        }
    }

    override suspend fun signOut() {
        Log.d("AuthRepository", "Signing out user: ${auth.currentUser?.email}")
        auth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        val user = auth.currentUser
        Log.d("AuthRepository", "Getting current user: ${user?.email}")
        return user
    }
} 