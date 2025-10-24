package com.qodein.shared.common.error

/**
 * Domain errors for Storage operations (image/file uploads).
 * Only storage-specific failures - use SystemError for infrastructure issues.
 */
sealed interface StorageError : OperationError {

    /**
     * Upload operation failures specific to storage
     */
    sealed interface UploadFailure : StorageError {
        data object NotAuthenticated : UploadFailure
        data object QuotaExceeded : UploadFailure
        data object FileTooLarge : UploadFailure
        data object InvalidFileType : UploadFailure
        data object UploadCancelled : UploadFailure
        data object CorruptedFile : UploadFailure
    }

    /**
     * Download/retrieval failures specific to storage
     */
    sealed interface RetrievalFailure : StorageError {
        data object FileNotFound : RetrievalFailure
    }

    /**
     * Delete operation failures specific to storage
     */
    sealed interface DeletionFailure : StorageError {
        data object FileNotFound : DeletionFailure
    }
}
