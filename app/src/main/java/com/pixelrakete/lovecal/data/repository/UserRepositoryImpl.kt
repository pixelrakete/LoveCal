package com.pixelrakete.lovecal.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelrakete.lovecal.data.model.UserInfo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getCurrentUserInfo(): UserInfo {
        val currentUser = auth.currentUser ?: throw IllegalStateException("No user logged in")
        
        val userDoc = firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .await()

        return UserInfo(
            id = currentUser.uid,
            name = userDoc.getString("name") ?: "",
            email = currentUser.email ?: "",
            color = userDoc.getString("color") ?: "#FF0000",
            partnerColor = userDoc.getString("partnerColor") ?: "#0000FF"
        )
    }
} 