package com.qodein.shared.common.logging

import co.touchlab.kermit.Logger
import com.qodein.shared.common.logging.logPerformance
import com.qodein.shared.common.logging.logUserAction

/**
 * Simple logging test to verify Kermit -> Timber bridge works.
 * This file can be deleted after testing - it's just for demonstration.
 */
object LoggingTest {
    private val logger = Logger.withTag("LoggingTest")

    fun testLogging() {
        logger.d { "Testing Kermit logging from shared module" }
        logger.i { "This should appear in Timber output on Android" }

        // Test extension functions
        logger.logUserAction("test_action", mapOf("test" to "value"))
        logger.logPerformance("test_operation", 150)

        // Test error logging (simulated)
        try {
            throw RuntimeException("Test error for logging demonstration")
        } catch (e: Exception) {
            logger.logHandledError(e)
        }
    }
}
