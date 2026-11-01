package com.qodein.shared.data.util

import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder

/** Position after the last row of a page. `id` breaks ties between equal sort values. */
data class PageCursor(val sortValue: String, val id: String)

data class Page<T>(val items: List<T>, val nextCursor: PageCursor?)

/** Callers fetch `limit + 1` rows, so an extra row means another page exists. */
fun <T> List<T>.toPage(
    limit: Int,
    cursorOf: (T) -> PageCursor
): Page<T> {
    val items = take(limit)
    return Page(items, if (size > limit) items.lastOrNull()?.let(cursorOf) else null)
}

fun PostgrestFilterBuilder.startAfter(
    cursor: PageCursor,
    column: String,
    ascending: Boolean,
    idColumn: String = "id"
) {
    or {
        if (ascending) gt(column, cursor.sortValue) else lt(column, cursor.sortValue)
        and {
            eq(column, cursor.sortValue)
            gt(idColumn, cursor.id)
        }
    }
}

fun PostgrestFilterBuilder.notIn(
    column: String,
    values: Collection<String>
) {
    if (values.isNotEmpty()) filterNot(column, FilterOperator.IN, values.toInList())
}

fun PostgrestFilterBuilder.within(
    column: String,
    values: Collection<String>
) {
    filter(column, FilterOperator.IN, values.toInList())
}

private fun Collection<String>.toInList(): String =
    joinToString(separator = ",", prefix = "(", postfix = ")") { value ->
        "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
    }
