package com.qodein.core.analytics

fun AnalyticsHelper.logLogin(
    method: String,
    success: Boolean
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.LOGIN,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.METHOD, method),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SUCCESS, success.toString()),
            ),
        ),
    )
}

fun AnalyticsHelper.logLogout() {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.LOGOUT,
            extras = emptyList(),
        ),
    )
}

fun AnalyticsHelper.logPromoCodeView(
    promocodeId: String,
    promocodeType: String
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.VIEW_PROMOCODE,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.PROMOCODE_ID, promocodeId),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.PROMOCODE_TYPE, promocodeType),
            ),
        ),
    )
}

fun AnalyticsHelper.logPromoCodeSubmission(
    promocodeId: String,
    promocodeType: String,
    success: Boolean
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SUBMIT_PROMOCODE,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.PROMOCODE_ID, promocodeId),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.PROMOCODE_TYPE, promocodeType),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SUCCESS, success.toString()),
            ),
        ),
    )
}

fun AnalyticsHelper.logPostSubmission(
    postId: String?,
    success: Boolean
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SUBMIT_POST,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.POST_ID, postId ?: "null"),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SUCCESS, success.toString()),
            ),
        ),
    )
}

fun AnalyticsHelper.logVote(
    promocodeId: String,
    voteType: String
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.VOTE,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.PROMOCODE_ID, promocodeId),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.VOTE_TYPE, voteType),
            ),
        ),
    )
}

fun AnalyticsHelper.logSearch(searchTerm: String) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SEARCH,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SEARCH_TERM, searchTerm),
            ),
        ),
    )
}

fun AnalyticsHelper.logFilterContent(
    filterType: String,
    filterValue: String
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.FILTER_CONTENT,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.FILTER_TYPE, filterType),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.FILTER_VALUE, filterValue),
            ),
        ),
    )
}

fun AnalyticsHelper.logScreenView(
    screenName: String,
    screenClass: String? = null
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SCREEN_VIEW,
            extras = buildList {
                add(AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SCREEN_NAME, screenName))
                screenClass?.let {
                    add(AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SCREEN_CLASS, it))
                }
            },
        ),
    )
}

fun AnalyticsHelper.logTabSwitch(tabName: String) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SELECT_CONTENT,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.CONTENT_TYPE, "tab"),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.ITEM_ID, tabName),
            ),
        ),
    )
}

fun AnalyticsHelper.logCopyPromoCode(
    promocodeId: String,
    promocodeType: String
) {
    logEvent(
        AnalyticsEvent(
            type = "copy_promocode",
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.PROMOCODE_ID, promocodeId),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.PROMOCODE_TYPE, promocodeType),
            ),
        ),
    )
}

fun AnalyticsHelper.logMessageRead(messageId: String) {
    logEvent(
        AnalyticsEvent(
            type = "mark_message_read",
            extras = listOf(
                AnalyticsEvent.Param("message_id", messageId),
            ),
        ),
    )
}

fun AnalyticsHelper.logMessageDelete(messageId: String) {
    logEvent(
        AnalyticsEvent(
            type = "delete_message",
            extras = listOf(
                AnalyticsEvent.Param("message_id", messageId),
            ),
        ),
    )
}

fun AnalyticsHelper.logProfileAction(action: String) {
    logEvent(
        AnalyticsEvent(
            type = "profile_action",
            extras = listOf(
                AnalyticsEvent.Param("action", action),
            ),
        ),
    )
}

fun AnalyticsHelper.logWizardStepNavigation(
    stepFrom: String,
    stepTo: String,
    direction: String
) {
    logEvent(
        AnalyticsEvent(
            type = "wizard_step_navigation",
            extras = listOf(
                AnalyticsEvent.Param("step_from", stepFrom),
                AnalyticsEvent.Param("step_to", stepTo),
                AnalyticsEvent.Param("direction", direction),
            ),
        ),
    )
}
