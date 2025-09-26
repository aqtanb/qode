package com.qodein.shared.common.error

/**
 * Base marker interface for all errors in the application.
 */
sealed interface Error

/**
 * Union type for all operation errors in the application.
 * Allows repositories to return any error type while maintaining type safety.
 */
sealed interface OperationError : Error
