package com.bookpalace.app.viewmodel

import com.bookpalace.app.model.User
import com.bookpalace.app.model.UserRole
import com.google.firebase.database.*

class UserViewModel {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    // Find user by ID
    fun findById(id: String, callback: (User?) -> Unit) {
        database.child(id).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            callback(user)
        }.addOnFailureListener {
            callback(null)
        }
    }

    // Find user by email
    fun findByEmail(email: String, callback: (User?) -> Unit) {
        database.orderByChild("email").equalTo(email).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.children.firstOrNull()?.getValue(User::class.java)
                callback(user)
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    // Get all users
    fun findAll(callback: (List<User>) -> Unit) {
        database.get().addOnSuccessListener { snapshot ->
            val userList = mutableListOf<User>()
            for (doc in snapshot.children) {
                val user = doc.getValue(User::class.java)
                user?.let { userList.add(it) }
            }
            callback(userList)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    // Find users by role
    fun findByRole(role: UserRole, callback: (List<User>) -> Unit) {
        database.orderByChild("role").equalTo(role.name).get().addOnSuccessListener { snapshot ->
            val userList = mutableListOf<User>()
            for (doc in snapshot.children) {
                val user = doc.getValue(User::class.java)
                user?.let { userList.add(it) }
            }
            callback(userList)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    // Save new user
    fun save(user: User, callback: (Boolean) -> Unit) {
        val id = database.push().key ?: return callback(false)
        val newUser = user.copy(id = id)

        database.child(id).setValue(newUser)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Update user
    fun update(user: User, callback: (Boolean) -> Unit) {
        if (user.id.isEmpty()) return callback(false)
        
        database.child(user.id).setValue(user)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Delete user
    fun deleteById(id: String, callback: (Boolean) -> Unit) {
        database.child(id).removeValue()
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Check if email exists
    fun existsByEmail(email: String, callback: (Boolean) -> Unit) {
        database.orderByChild("email").equalTo(email).get().addOnSuccessListener { snapshot ->
            callback(snapshot.exists())
        }.addOnFailureListener {
            callback(false)
        }
    }
}
