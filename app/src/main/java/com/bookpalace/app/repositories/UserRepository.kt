package com.bookpalace.app.repositories

import com.bookpalace.app.model.User
import com.bookpalace.app.model.UserRole


interface UserRepository {
    fun findById(id: String): User?
    fun findByEmail(email: String): User?
    fun findAll(): List<User>
    fun findByRole(role: UserRole): List<User>
    fun save(user: User): User
    fun update(user: User): User
    fun deleteById(id: String): Boolean
    fun existsByEmail(email: String): Boolean
}
