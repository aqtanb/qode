package com.qodein.core.analytics

/**
 * Analytics event data following NIA patterns.
 *
 * @param type - the event type. Wherever possible use one of the standard
 * event types (e.g. screen_view) but if you need something custom, use a
 * descriptive event type name, preferably following a naming convention such as
 * noun_verb, e.g. "authentication_succeeded".
 * @param extras - list of parameters which supply additional context to the event.
 * Again, use standard parameter names wherever possible, but if you need something
 * custom, use a descriptive name.
 */
data class AnalyticsEvent(val type: String, val extras: List<Param> = emptyList()) {
    // Standard event types
    object Types {
        const val SCREEN_VIEW = "screen_view"
        const val USER_ENGAGEMENT = "user_engagement"
        const val LOGIN = "login"
        const val LOGOUT = "logout"
        const val SIGN_UP = "sign_up"
        const val SEARCH = "search"
        const val SELECT_CONTENT = "select_content"
        const val SHARE = "share"
        const val VOTE = "vote"
        const val SUBMIT_PROMOCODE = "submit_promocode"
        const val VIEW_PROMOCODE = "view_promocode"
        const val FILTER_CONTENT = "filter_content"
        const val TAB_SWITCH = "tab_switch"
    }

    /**
     * A key-value pair used to supply extra context to an analytics event.
     *
     * @param key - the parameter key. Wherever possible use one of the standard
     * event parameter names.
     * @param value - the parameter value.
     */
    data class Param(val key: String, val value: String)

    // Standard event parameter keys
    object ParamKeys {
        const val SCREEN_NAME = "screen_name"
        const val SCREEN_CLASS = "screen_class"
        const val USER_ID = "user_id"
        const val CONTENT_TYPE = "content_type"
        const val ITEM_ID = "item_id"
        const val ITEM_NAME = "item_name"
        const val SEARCH_TERM = "search_term"
        const val VALUE = "value"
        const val SUCCESS = "success"
        const val METHOD = "method"
        const val CATEGORY = "category"
        const val STORE = "store"
        const val FILTER_TYPE = "filter_type"
        const val FILTER_VALUE = "filter_value"
        const val TAB_NAME = "tab_name"
        const val PROMOCODE_ID = "promocode_id"
        const val PROMOCODE_TYPE = "promocode_type"
        const val VOTE_TYPE = "vote_type"
        const val ERROR_MESSAGE = "error_message"
        const val ENGAGEMENT_TIME_MSEC = "engagement_time_msec"
    }
}
