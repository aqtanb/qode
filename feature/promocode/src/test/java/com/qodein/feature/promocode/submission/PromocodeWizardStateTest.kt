package com.qodein.feature.promocode.submission

import com.qodein.core.testing.data.ServiceMother
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PromocodeWizardStateTest {

    private val today = LocalDate.of(2026, 11, 8)

    private val complete = SubmissionWizardData(
        selectedService = ServiceMother.netflix(),
        promocode = "SAVE20",
        promocodeType = PromocodeType.PERCENTAGE,
        discountPercentage = "20",
        minimumOrderAmount = "5000",
        startDate = today,
        endDate = today.plusDays(30),
    )

    @Test
    fun serviceStepNeedsSelectedServiceOrBothManualFields() {
        val step = PromocodeWizardStep.SERVICE

        assertFalse(step.canProceed(SubmissionWizardData()))
        assertFalse(step.canProceed(SubmissionWizardData(serviceName = "Kaspi Shop")))
        assertTrue(step.canProceed(SubmissionWizardData(serviceName = "Kaspi Shop", serviceUrl = "kaspi.kz")))
        assertTrue(step.canProceed(SubmissionWizardData(selectedService = ServiceMother.netflix())))
    }

    @Test
    fun promocodeStepNeedsCodeAndType() {
        val step = PromocodeWizardStep.PROMOCODE

        assertFalse(step.canProceed(complete.copy(promocode = "  ")))
        assertFalse(step.canProceed(complete.copy(promocodeType = null)))
        assertTrue(step.canProceed(complete))
    }

    @Test
    fun discountStepChecksTheFieldOfTheChosenType() {
        val step = PromocodeWizardStep.DISCOUNT_VALUE
        val fixed = complete.copy(promocodeType = PromocodeType.FIXED_AMOUNT, discountPercentage = "")

        assertFalse(step.canProceed(fixed))
        assertTrue(step.canProceed(fixed.copy(discountAmount = "1000")))
        assertTrue(step.canProceed(complete.copy(promocodeType = PromocodeType.FREE_ITEM, freeItemDescription = "Coffee")))
        assertFalse(step.canProceed(complete.copy(minimumOrderAmount = "")))
    }

    @Test
    fun datesStepNeedsEndAfterStart() {
        val step = PromocodeWizardStep.DATES

        assertFalse(step.canProceed(complete.copy(endDate = null)))
        assertFalse(step.canProceed(complete.copy(endDate = today)))
        assertTrue(step.canProceed(complete))
    }

    @Test
    fun stepsWalkForwardAndBackInOrder() {
        val forward = generateSequence(PromocodeWizardStep.SERVICE) { it.next() }.toList()

        assertEquals(PromocodeWizardStep.entries, forward)
        assertEquals(forward.reversed(), generateSequence(PromocodeWizardStep.DESCRIPTION) { it.previous() }.toList())
        assertNull(PromocodeWizardStep.DESCRIPTION.next())
    }

    @Test
    fun cannotSubmitBeforeReachingTheDatesStep() {
        val state = PromocodeSubmissionUiState(currentStep = PromocodeWizardStep.DISCOUNT_VALUE, wizardData = complete)

        assertFalse(state.canSubmit)
        assertTrue(state.copy(currentStep = PromocodeWizardStep.DATES).canSubmit)
    }

    @Test
    fun cannotSubmitWhileCompressingOrWithAnIncompleteEarlierStep() {
        val state = PromocodeSubmissionUiState(currentStep = PromocodeWizardStep.DESCRIPTION, wizardData = complete)

        assertTrue(state.canSubmit)
        assertFalse(state.copy(isCompressing = true).canSubmit)
        assertFalse(state.copy(wizardData = complete.copy(promocode = "")).canSubmit)
    }
}
