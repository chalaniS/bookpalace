package com.bookpalace.app.model

import java.time.LocalDateTime

enum class UserRole {
    STUDENT, LIBRARIAN, OWNER
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val phone: String,
    val address: String,
    val registeredAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true
)
