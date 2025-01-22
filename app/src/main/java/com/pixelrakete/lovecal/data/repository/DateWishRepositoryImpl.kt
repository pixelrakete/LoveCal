package com.pixelrakete.lovecal.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.model.DateWish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random

class DateWishRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userManager: UserManager
) : DateWishRepository {

    override suspend fun getRandomWish(): DateWish? {
        val userId = userManager.getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        
        val wishes = firestore.collection("dateWishes")
            .whereEqualTo("createdBy", userId)
            .get()
            .await()
            .toObjects(DateWish::class.java)

        return wishes.randomOrNull()
    }

    override suspend fun saveDateWish(title: String, description: String) {
        val userId = userManager.getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        
        val wish = DateWish(
            title = title,
            description = description,
            createdBy = userId,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("dateWishes")
            .add(wish)
            .await()
    }

    override suspend fun deleteDateWish(wishId: String) {
        firestore.collection("dateWishes")
            .document(wishId)
            .delete()
            .await()
    }

    override fun getDateWishes(): Flow<List<DateWish>> = flow {
        val userId = userManager.getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        
        val wishes = firestore.collection("dateWishes")
            .whereEqualTo("createdBy", userId)
            .get()
            .await()
            .toObjects(DateWish::class.java)
        
        emit(wishes)
    }
} 