package com.qodein.core.analytics

import com.qodein.core.analytics.AnalyticsEvent.ParamKeys
import com.qodein.core.analytics.AnalyticsEvent.Types

/**
 * Domain-specific extension functions for common analytics events.
 */

/**
 * Log a screen view event.
 */
fun AnalyticsHelper.logScreenView(
    screenName: String,
    screenClass: String? = null
) {
    val params = mutableListOf<AnalyticsEvent.Param>().apply {
        add(AnalyticsEvent.Param(ParamKeys.SCREEN_NAME, screenName))
        screenClass?.let { add(AnalyticsEvent.Param(ParamKeys.SCREEN_CLASS, it)) }
    }
    logEvent(AnalyticsEvent(Types.SCREEN_VIEW, params))
}

/**
 * Log a promocode submission event.
 */
fun AnalyticsHelper.logPromoCodeSubmission(
    promocodeId: String,
    promocodeType: String,
    success: Boolean
) {
    logEvent(
        AnalyticsEvent(
            type = Types.SUBMIT_PROMOCODE,
            extras = listOf(
                AnalyticsEvent.Param(ParamKeys.PROMOCODE_ID, promocodeId),
                AnalyticsEvent.Param(ParamKeys.PROMOCODE_TYPE, promocodeType),
                AnalyticsEvent.Param(ParamKeys.SUCCESS, success.toString()),
            ),
        ),
    )
}

/**
 * Log a promocode view event.
 */
fun AnalyticsHelper.logPromoCodeView(
    promocodeId: String,
    promocodeType: String
) {
    logEvent(
        AnalyticsEvent(
            type = Types.VIEW_PROMOCODE,
            extras = listOf(
                AnalyticsEvent.Param(ParamKeys.PROMOCODE_ID, promocodeId),
                AnalyticsEvent.Param(ParamKeys.PROMOCODE_TYPE, promocodeType),
            ),
        ),
    )
}

/**
 * Log a vote event.
 */
fun AnalyticsHelper.logVote(
    promocodeId: String,
    voteType: String // "upvote" or "downvote"
) {
    logEvent(
        AnalyticsEvent(
            type = Types.VOTE,
            extras = listOf(
                AnalyticsEvent.Param(ParamKeys.PROMOCODE_ID, promocodeId),
                AnalyticsEvent.Param(ParamKeys.VOTE_TYPE, voteType),
            ),
        ),
    )
}

/**
 * Log a search event.
 */
fun AnalyticsHelper.logSearch(searchTerm: String) {
    logEvent(
        AnalyticsEvent(
            type = Types.SEARCH,
            extras = listOf(
                AnalyticsEvent.Param(ParamKeys.SEARCH_TERM, searchTerm),
            ),
        ),
    )
}

/**
 * Log a filter event.
 */
fun AnalyticsHelper.logFilterContent(
    filterType: String,
    filterValue: String
) {
    logEvent(
        AnalyticsEvent(
            type = Types.FILTER_CONTENT,
            extras = listOf(
                AnalyticsEvent.Param(ParamKeys.FILTER_TYPE, filterType),
                AnalyticsEvent.Param(ParamKeys.FILTER_VALUE, filterValue),
            ),
        ),
    )
}

/**
 * Log a tab switch event.
 */
fun AnalyticsHelper.logTabSwitch(tabName: String) {
    logEvent(
        AnalyticsEvent(
            type = Types.TAB_SWITCH,
            extras = listOf(
                AnalyticsEvent.Param(ParamKeys.TAB_NAME, tabName),
            ),
        ),
    )
}

/**
 * Log a login event.
 */
fun AnalyticsHelper.logLogin(
    method: String,
    success: Boolean
) {
    logEvent(
        AnalyticsEvent(
            type = Types.LOGIN,
            extras = listOf(
                AnalyticsEvent.Param(ParamKeys.METHOD, method),
                AnalyticsEvent.Param(ParamKeys.SUCCESS, success.toString()),
            ),
        ),
    )
}

/**
 * Log a logout event.
 */
fun AnalyticsHelper.logLogout() {
    logEvent(AnalyticsEvent(Types.LOGOUT))
}

/**
 * Log a sign up event.
 */
fun AnalyticsHelper.logSignUp(
    method: String,
    success: Boolean
) {
    logEvent(
        AnalyticsEvent(
            type = Types.SIGN_UP,
            extras = listOf(
                AnalyticsEvent.Param(ParamKeys.METHOD, method),
                AnalyticsEvent.Param(ParamKeys.SUCCESS, success.toString()),
            ),
        ),
    )
}
