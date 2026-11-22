package com.qodein.shared.ios

import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.usecase.post.SubmitPostUseCase
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeRequest
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.ServiceId
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.User
import com.qodein.shared.platform.PlatformUri
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Instant

/** Submits promocodes and posts in the foreground. Image paths are local JPEG files. */
class SubmitApi : KoinComponent {
    private val authRepository: AuthRepository by inject()
    private val getUserById: GetUserByIdUseCase by inject()
    private val submitPromocodeUseCase: SubmitPromocodeUseCase by inject()
    private val submitPostUseCase: SubmitPostUseCase by inject()

    /** Pass [serviceId] for an existing service, otherwise [serviceName] and [serviceSite] to find or create one. */
    @Throws(Exception::class)
    suspend fun submitPromocode(
        code: String,
        serviceId: String?,
        serviceName: String,
        serviceSite: String,
        discountKind: DiscountKind,
        discountValue: Double,
        freeItem: String,
        minimumOrderAmount: Double,
        startEpochMillis: Long,
        endEpochMillis: Long,
        description: String?,
        imagePaths: List<String>,
        onProgress: (Int, Int) -> Unit
    ): String {
        val user = currentUser()
        val discount = when (discountKind) {
            DiscountKind.PERCENTAGE -> Discount.Percentage(discountValue)
            DiscountKind.FIXED_AMOUNT -> Discount.FixedAmount(discountValue)
            DiscountKind.FREE_ITEM -> Discount.FreeItem(freeItem.trim())
        }
        val service = serviceId?.let { ServiceRef.ById(ServiceId(it)) } ?: ServiceRef.ByName(serviceName.trim(), serviceSite.trim())
        val request = SubmitPromocodeRequest(
            code = code,
            service = service,
            currentUser = user,
            discount = discount,
            minimumOrderAmount = minimumOrderAmount,
            startDate = Instant.fromEpochMilliseconds(startEpochMillis),
            endDate = Instant.fromEpochMilliseconds(endEpochMillis),
            description = description?.trim()?.ifBlank { null },
            imageUris = imagePaths,
            isFirstUserOnly = false,
            isOneTimeUseOnly = false,
            isVerified = false,
        )
        return call { submitPromocodeUseCase(request, imagePaths.map { PlatformUri(it) }, onProgress) }
    }

    @Throws(Exception::class)
    suspend fun submitPost(
        title: String,
        content: String,
        tags: List<String>,
        imagePaths: List<String>,
        onProgress: (Int, Int) -> Unit
    ): String {
        val user = currentUser()
        return call {
            submitPostUseCase(
                authorId = user.id,
                authorName = user.displayName ?: "",
                title = title,
                content = content,
                imageUris = imagePaths.map { PlatformUri(it) },
                tags = tags,
                authorAvatarUrl = user.profile.photoUrl,
                onProgress = onProgress,
            )
        }
    }

    private suspend fun currentUser(): User {
        val uid = authRepository.observeAuthState().first()?.uid ?: throw QodeException("NoCredentialsAvailable", "Not signed in")
        return call { getUserById(uid) }
    }
}
