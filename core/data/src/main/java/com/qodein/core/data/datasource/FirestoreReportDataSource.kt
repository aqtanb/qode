package com.qodein.core.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.qodein.core.data.dto.ContentReportDto
import kotlinx.coroutines.tasks.await

class FirestoreReportDataSource(private val firestore: FirebaseFirestore) {
    suspend fun submitReport(dto: ContentReportDto) {
        firestore.collection(ContentReportDto.COLLECTION_NAME)
            .document()
            .set(dto)
            .await()
    }
}
