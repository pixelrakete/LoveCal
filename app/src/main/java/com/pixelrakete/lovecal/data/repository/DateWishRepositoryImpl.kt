package com.pixelrakete.lovecal.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pixelrakete.lovecal.data.model.DateWish
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class DateWishRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DateWishRepository {

    private val wishesCollection = firestore.collection("date_wishes")

    override suspend fun getDateWishes(userId: String): List<DateWish> {
        return try {
            val snapshot = wishesCollection
                .whereEqualTo("createdBy", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                DateWish(
                    id = doc.id,
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    location = data["location"] as? String,
                    budget = (data["budget"] as? Number)?.toDouble(),
                    createdBy = data["createdBy"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveDateWish(wish: DateWish) {
        val wishMap = mapOf(
            "title" to wish.title,
            "description" to wish.description,
            "location" to wish.location,
            "budget" to wish.budget,
            "createdBy" to wish.createdBy
        )
        wishesCollection.document(wish.id).set(wishMap).await()
    }

    override suspend fun addDateWish(
        title: String,
        description: String,
        location: String?,
        budget: Double?,
        createdBy: String
    ) {
        val wish = DateWish(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            location = location,
            budget = budget,
            createdBy = createdBy
        )
        saveDateWish(wish)
    }

    override suspend fun deleteDateWish(id: String) {
        wishesCollection.document(id).delete().await()
    }
} 