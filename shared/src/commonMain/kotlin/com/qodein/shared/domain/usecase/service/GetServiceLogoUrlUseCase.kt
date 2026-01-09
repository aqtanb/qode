package com.qodein.shared.domain.usecase.service

import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.ServiceError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.config.AppConfig
import com.qodein.shared.model.Service.Companion.isValidUrl
import com.qodein.shared.model.Service.Companion.sanitizeUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse

class GetServiceLogoUrlUseCase(private val httpClient: HttpClient, private val logoToken: String = AppConfig.logoDevKey) {
    suspend operator fun invoke(domain: String): Result<String, OperationError> {
        val sanitizedDomain = sanitizeUrl(domain)

        if (!isValidUrl(sanitizedDomain)) {
            return Result.Error(ServiceError.CreationFailure.InvalidDomainFormat)
        }

        return try {
            val logoUrl = "https://img.logo.dev/$sanitizedDomain?token=$logoToken&size=300&retina=true&fallback=404"

            val response: HttpResponse = httpClient.get(logoUrl) {
                header("User-Agent", "Mozilla/5.0 (Android)")
            }

            when (response.status.value) {
                200 -> Result.Success(logoUrl)
                404 -> Result.Error(ServiceError.CreationFailure.LogoNotFound)
                else -> Result.Error(SystemError.Unknown)
            }
        } catch (e: Exception) {
            Logger.e(e) { "Error validating logo for $domain" }
            Result.Error(SystemError.Offline)
        }
    }
}
