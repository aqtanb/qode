package com.qodein.core.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.dto.BannerDto
import kotlinx.coroutines.tasks.await

class FirestoreBannerDataSource(private val firestore: FirebaseFirestore) {
    suspend fun getBanners(limit: Long): List<BannerDto> =
        firestore.collection(BannerDto.COLLECTION_NAME)
            .orderBy(BannerDto.FIELD_PRIORITY, Query.Direction.DESCENDING)
            .whereEqualTo(BannerDto.FIELD_IS_ACTIVE, true)
            .whereGreaterThan(BannerDto.FIELD_EXPIRES_AT, Timestamp.now())
            .limit(limit)
            .get()
            .await()
            .documents.mapNotNull { it.toObject<BannerDto>() }
}
