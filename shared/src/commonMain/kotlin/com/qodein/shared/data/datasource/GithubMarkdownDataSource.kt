package com.qodein.shared.data.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

private const val BASE_URL = "https://raw.githubusercontent.com/aqtanb/qode/refs/heads/main/"
internal const val TERMS_URL = "${BASE_URL}TERMS_OF_SERVICE.md"
internal const val PRIVACY_URL = "${BASE_URL}PRIVACY_POLICY.md"

class GithubMarkdownDataSource(private val httpClient: HttpClient) {
    suspend fun fetchMarkdown(url: String): String = httpClient.get(url).bodyAsText()
}
