package com.qodein.core.ui.provider

import android.content.Context
import com.qodein.core.ui.R
import com.qodein.shared.domain.provider.ShareStringProvider

/**
 * Android implementation of ShareStringProvider
 * Uses Context to access string resources
 */
class AndroidShareStringProvider(private val context: Context) : ShareStringProvider {

    override fun getPostShareHeader(postTitle: String): String = context.getString(R.string.ui_share_post_header, postTitle)

    override fun getAuthorAttribution(authorName: String): String = context.getString(R.string.ui_share_author_attribution, authorName)

    override fun getPostShareCallToAction(): String = context.getString(R.string.ui_share_post_cta)

    override fun getPromocodeShareHeader(
        serviceName: String,
        discount: String
    ): String = context.getString(R.string.ui_share_promocode_header, serviceName, discount)

    override fun getPromocodeLabel(code: String): String = context.getString(R.string.ui_share_code_label, code)
}
