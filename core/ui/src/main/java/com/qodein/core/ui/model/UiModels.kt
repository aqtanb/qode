package com.qodein.core.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * UI data models for Qode app components
 */

data class Store(
    val id: String,
    val name: String,
    val logo: String? = null,
    val website: String? = null,
    val category: StoreCategory = StoreCategory.Other,
    val followersCount: Int = 0,
    val isFollowed: Boolean = false
)

data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector = Icons.Default.Category,
    val color: Color = Color.Gray,
    val followersCount: Int = 0,
    val isFollowed: Boolean = false
)

data class User(
    val id: String,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val totalUpvotes: Int = 0,
    val submittedPromoCodes: Int = 0,
    val followedStores: List<String> = emptyList(),
    val followedCategories: List<String> = emptyList(),
    val joinedAt: LocalDateTime
)

data class PromoCode(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val store: Store,
    val category: Category,
    val minimumOrderAmount: Int? = null, // in tenge
    val discountAmount: Int? = null, // in tenge
    val discountPercentage: Int? = null, // 0-100
    val isFirstOrderOnly: Boolean = false,
    val isSingleUse: Boolean = false,
    val expiryDate: LocalDate? = null,
    val upvotes: Int = 0,
    val isVerified: Boolean = false,
    val submittedBy: User? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime = createdAt,
    val isUpvoted: Boolean = false,
    val isUsed: Boolean = false // For single-use tracking
)

data class Comment(
    val id: String,
    val promoCodeId: String,
    val userId: String,
    val username: String,
    val userAvatarUrl: String? = null,
    val parentCommentId: String? = null,
    val content: String,
    val upvotes: Int = 0,
    val isUpvoted: Boolean = false,
    val createdAt: LocalDateTime,
    val replies: List<Comment> = emptyList()
)

enum class StoreCategory {
    Electronics,
    Fashion,
    Food,
    Beauty,
    Sports,
    Home,
    Books,
    Travel,
    Other
}

data class UserPromoCodeUsage(
    val userId: String,
    val promoCodeId: String,
    val usedAt: LocalDateTime
)
