package com.qodein.feature.promocode.submission

enum class ProgressiveStep(val stepNumber: Int, val hint: String) {
    SERVICE(1, "Select the service for your promo code. Can't find it? Type it manually.") {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.serviceName.isNotBlank()
    },
    DISCOUNT_TYPE(2, "Percentage takes % off the total. " + "\nFixed amount takes exact $ off.") {

        override fun canProceed(data: SubmissionWizardData): Boolean = data.promoCodeType != null
    },
    PROMO_CODE(3, "Enter your promo code (e.g., SAVE20).") {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.promoCode.isNotBlank()
    },
    DISCOUNT_VALUE(4, "Set your discount value and minimum order amount.") {
        override fun canProceed(data: SubmissionWizardData): Boolean =
            when (data.promoCodeType) {
                PromoCodeType.PERCENTAGE -> data.discountPercentage.isNotBlank() && data.minimumOrderAmount.isNotBlank()
                PromoCodeType.FIXED_AMOUNT -> data.discountAmount.isNotBlank() && data.minimumOrderAmount.isNotBlank()
                null -> false
            }
    },
    START_DATE(5, "Set when your promo code becomes active. Default is today.") {
        override fun canProceed(data: SubmissionWizardData): Boolean = true // Always valid (defaults to today)
    },
    END_DATE(6, "Choose when your promo code expires. This is required.") {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.endDate != null && data.endDate.isAfter(data.startDate)
    },
    OPTIONS(7, "Configure additional options for your promo code.") {
        override fun canProceed(data: SubmissionWizardData): Boolean = true // Optional step, always valid
    };

    abstract fun canProceed(data: SubmissionWizardData): Boolean

    val isFirst: Boolean get() = this == SERVICE
    val isLast: Boolean get() = this == OPTIONS

    fun next(): ProgressiveStep? =
        when (this) {
            SERVICE -> DISCOUNT_TYPE
            DISCOUNT_TYPE -> PROMO_CODE
            PROMO_CODE -> DISCOUNT_VALUE
            DISCOUNT_VALUE -> START_DATE
            START_DATE -> END_DATE
            END_DATE -> OPTIONS
            OPTIONS -> null
        }

    fun previous(): ProgressiveStep? =
        when (this) {
            SERVICE -> null
            DISCOUNT_TYPE -> SERVICE
            PROMO_CODE -> DISCOUNT_TYPE
            DISCOUNT_VALUE -> PROMO_CODE
            START_DATE -> DISCOUNT_VALUE
            END_DATE -> START_DATE
            OPTIONS -> END_DATE
        }
}
