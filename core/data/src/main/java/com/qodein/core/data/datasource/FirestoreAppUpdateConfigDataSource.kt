package com.qodein.core.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.qodein.core.data.dto.AppUpdateConfigDto
import com.qodein.core.data.dto.AppUpdateConfigDto.Companion.COLLECTION_NAME
import com.qodein.core.data.dto.AppUpdateConfigDto.Companion.FIELD_UPDATE_POLICY
import kotlinx.coroutines.tasks.await

class FirestoreAppUpdateConfigDataSource(private val firestore: FirebaseFirestore) {
    internal suspend fun getAppUpdateConfig(): AppUpdateConfigDto? =
        firestore.collection(COLLECTION_NAME).document(FIELD_UPDATE_POLICY).get().await().toObject<AppUpdateConfigDto>()
}
