package com.qodein.shared.domain.usecase.promo

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoRepository
import com.qodein.shared.model.Promo
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

class CreatePromoUseCase constructor(private val promoRepository: PromoRepository) {
    operator fun invoke(
        description: String,
        serviceName: String,
        createdBy: UserId,
        imageUrls: List<String> = emptyList(),
        category: String? = null,
        targetCountries: List<String> = emptyList(),
        expiresAt: Instant? = null
    ): Flow<Result<Promo>> {
        require(description.isNotBlank()) { "Promo description cannot be blank" }
        require(description.length <= 1000) { "Promo description cannot exceed 1000 characters" }
        require(serviceName.isNotBlank()) { "Service name cannot be blank" }
        require(imageUrls.size <= 5) { "Promo cannot have more than 5 images" }

        val promo = Promo.create(
            description = description.trim(),
            serviceName = serviceName.trim(),
            createdBy = createdBy,
            imageUrls = imageUrls.map { it.trim() }.filter { it.isNotBlank() },
            category = category?.trim(),
            targetCountries = targetCountries.map { it.uppercase() }.filter { it.isNotBlank() },
            expiresAt = expiresAt,
        ).getOrThrow()

        return promoRepository.createPromo(promo).asResult()
    }
}
