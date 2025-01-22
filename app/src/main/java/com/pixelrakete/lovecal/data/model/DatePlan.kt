package com.pixelrakete.lovecal.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Exclude
import java.time.LocalDateTime
import java.time.ZoneOffset

data class DatePlan(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val location: String? = null,
    val budget: Double? = null,
    val dateTimeStr: String? = null,  // Format: "DD.MM.YYYY HH:mm"
    val createdBy: String = "",
    val coupleId: String = "",
    val isSurprise: Boolean = false,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    @get:PropertyName("dateTime")
    val dateTimeMillis: Long?
        get() = dateTimeStr?.let {
            it.split(" ").joinToString("").replace(".", "").toLongOrNull()
        }

    @set:PropertyName("dateTime")
    var dateTimeMillisForFirestore: Long?
        get() = dateTimeMillis
        set(value) {
            // This setter is required by Firestore but won't be used directly
            // The value will be handled by the constructor
        }

    constructor() : this(
        id = "",
        title = "",
        description = null,
        location = null,
        budget = null,
        dateTimeStr = null,
        createdBy = "",
        coupleId = "",
        isSurprise = false,
        completed = false,
        createdAt = System.currentTimeMillis()
    )

    companion object {
        fun fromFirestore(data: Map<String, Any?>): DatePlan {
            return DatePlan(
                id = data["id"] as? String ?: "",
                title = data["title"] as? String ?: "",
                description = data["description"] as? String,
                location = data["location"] as? String,
                budget = (data["budget"] as? Number)?.toDouble(),
                dateTimeStr = data["dateTime"] as? String,
                createdBy = data["createdBy"] as? String ?: "",
                coupleId = data["coupleId"] as? String ?: "",
                isSurprise = data["isSurprise"] as? Boolean ?: false,
                completed = data["completed"] as? Boolean ?: false,
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis()
            )
        }
    }
} 