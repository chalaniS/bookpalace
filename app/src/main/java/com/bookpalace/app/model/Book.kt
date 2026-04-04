package com.bookpalace.app.model

import java.time.LocalDateTime

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val categoryId: String,
    val publisher: String,
    val publishedYear: Int,
    val totalCopies: Int,
    val availableCopies: Int,
    val location: String,
    val coverImageUrl: String? = null,
    val description: String? = null,
    val addedBy: String,
    val addedAt: LocalDateTime = LocalDateTime.now(),
    val isDeleted: Boolean = false
)
