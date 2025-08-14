package com.qodein.qode.util

import com.qodein.core.data.util.SampleDataHelper
import com.qodein.core.domain.usecase.promocode.CreatePromoCodeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Initializes Firestore with sample data on first app launch
 */
@Singleton
class FirestoreDataInitializer @Inject constructor(
    private val sampleDataHelper: SampleDataHelper,
    private val createPromoCodeUseCase: CreatePromoCodeUseCase
) {

    private val initializerScope = CoroutineScope(SupervisorJob())

    fun initializeSampleData() {
        initializerScope.launch {
            val samplePromoCodes = sampleDataHelper.createSamplePromoCodes()

            samplePromoCodes.forEach { promoCode ->
                createPromoCodeUseCase(promoCode)
                    .catch { e ->
                        // Log error but continue with other promocodes
                        println("Failed to create promocode ${promoCode.code}: ${e.message}")
                    }
                    .collect { result ->
                        result.fold(
                            onSuccess = { createdPromoCode ->
                                println("Successfully created promocode: ${createdPromoCode.code} for ${createdPromoCode.serviceName}")
                            },
                            onFailure = { error ->
                                println("Failed to create promocode ${promoCode.code}: ${error.message}")
                            },
                        )
                    }
            }
        }
    }
}
