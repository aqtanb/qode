package com.qodein.core.data.repository

import co.touchlab.kermit.Logger
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.core.data.datasource.FirestoreBannerDataSource
import com.qodein.core.data.mapper.BannerMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.model.Banner

class BannerRepositoryImpl(private val dataSource: FirestoreBannerDataSource) : BannerRepository {

    override suspend fun getBanners(limit: Long): Result<List<Banner>, OperationError> =
        try {
            val rawBanners = dataSource.getBanners(limit)
            val domainBanners = rawBanners.map { BannerMapper.toDomain(it) }
            Logger.d { "Successfully fetched ${domainBanners.size} banners" }
            Result.Success(domainBanners)
        } catch (e: FirebaseFirestoreException) {
            val error = when (e.code) {
                FirebaseFirestoreException.Code.CANCELLED -> {
                    Logger.w(e) { "Banner query cancelled" }
                    FirestoreError.Cancelled
                }
                FirebaseFirestoreException.Code.INVALID_ARGUMENT -> {
                    Logger.e(e) { "Invalid query argument for banners" }
                    FirestoreError.InvalidArgument
                }
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                    Logger.e(e) { "Banner query timeout - consider optimizing query" }
                    FirestoreError.DeadlineExceeded
                }
                FirebaseFirestoreException.Code.NOT_FOUND -> {
                    Logger.w(e) { "Banners collection not found" }
                    FirestoreError.NotFound
                }
                FirebaseFirestoreException.Code.ALREADY_EXISTS -> {
                    Logger.e(e) { "Banner already exists" }
                    FirestoreError.AlreadyExists
                }
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Logger.e(e) { "Permission denied - check Firestore security rules for banners" }
                    FirestoreError.PermissionDenied
                }
                FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> {
                    Logger.e(e) { "Firestore quota exceeded" }
                    FirestoreError.ResourceExhausted
                }
                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                    Logger.e(e) { "Failed precondition - likely missing Firestore index. Check console for index creation link" }
                    FirestoreError.FailedPrecondition
                }
                FirebaseFirestoreException.Code.ABORTED -> {
                    Logger.w(e) { "Banner query aborted - concurrent modification" }
                    FirestoreError.Aborted
                }
                FirebaseFirestoreException.Code.OUT_OF_RANGE -> {
                    Logger.e(e) { "Query out of range" }
                    FirestoreError.OutOfRange
                }
                FirebaseFirestoreException.Code.UNIMPLEMENTED -> {
                    Logger.e(e) { "Operation not implemented" }
                    FirestoreError.Unimplemented
                }
                FirebaseFirestoreException.Code.INTERNAL -> {
                    Logger.e(e) { "Firestore internal error" }
                    FirestoreError.Internal
                }
                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                    Logger.w(e) { "Firestore unavailable - retry recommended" }
                    FirestoreError.Unavailable
                }
                FirebaseFirestoreException.Code.DATA_LOSS -> {
                    Logger.e(e) { "CRITICAL: Firestore data loss detected" }
                    FirestoreError.DataLoss
                }
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                    Logger.e(e) { "User not authenticated" }
                    FirestoreError.Unauthenticated
                }
                else -> {
                    Logger.e(e) { "Unknown Firestore error: ${e.code}" }
                    SystemError.Unknown
                }
            }
            Result.Error(error)
        } catch (e: FirebaseNetworkException) {
            Logger.e(e) { "Network error - no internet connection" }
            Result.Error(FirestoreError.Unavailable)
        } catch (e: Exception) {
            Logger.e(e) { "Unexpected error fetching banners: ${e.javaClass.simpleName}" }
            Result.Error(SystemError.Unknown)
        }
}
