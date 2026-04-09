package com.bookpalace.app.model

import java.io.Serializable

enum class UserRole {
    STUDENT, LIBRARIAN, OWNER
}

data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var passwordHash: String = "",
    var role: UserRole = UserRole.STUDENT,
    var phone: String = "",
    var address: String = "",
    var registeredAt: String = "",
    var isActive: Boolean = true
) : Serializable
