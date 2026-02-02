package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.DeeplinkConfig
import com.qodein.shared.domain.provider.ShareStringProvider
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.ShareContent

class GetPromocodeShareContentUseCase(
    private val getPromocodeUseCase: GetPromocodeUseCase,
    private val deeplinkConfig: DeeplinkConfig,
    private val stringProvider: ShareStringProvider
) {
    suspend operator fun invoke(promocodeId: PromocodeId): Result<ShareContent, OperationError> =
        when (val result = getPromocodeUseCase(promocodeId)) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> {
                val promocode = result.data
                val shareContent = buildShareContent(promocode)
                Result.Success(shareContent)
            }
        }

    private fun buildShareContent(promocode: Promocode): ShareContent {
        val discountText = formatDiscount(promocode.discount)

        val shareText = buildString {
            append(
                stringProvider.getPromocodeShareHeader(
                    promocode.serviceName,
                    discountText,
                ),
            )
            append("\n")

            append(stringProvider.getPromocodeLabel(promocode.code))
            append("\n\n")

            promocode.description?.takeIf { it.isNotBlank() }?.let { description ->
                append(description.trim())
                append("\n\n")
            }

            append(deeplinkConfig.getPromocodeWebUrl(promocode.id.value))
        }

        return ShareContent(
            title = "${promocode.serviceName} Promocode",
            text = shareText,
            url = deeplinkConfig.getPromocodeWebUrl(promocode.id.value),
        )
    }

    private fun formatDiscount(discount: Discount): String =
        when (discount) {
            is Discount.Percentage -> "${discount.value}%"
            is Discount.FixedAmount -> "${discount.value}₸"
            is Discount.FreeItem -> discount.description
        }
}
