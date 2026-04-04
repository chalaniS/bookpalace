package com.bookpalace.app.viewmodel

import com.bookpalace.app.model.User
import com.bookpalace.app.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class UserViewModel {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // Find user by ID
    fun findById(id: String, callback: (User?) -> Unit) {
        usersCollection.document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    // Find user by email
    fun findByEmail(email: String, callback: (User?) -> Unit) {
        usersCollection
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0].toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    // Get all users
    fun findAll(callback: (List<User>) -> Unit) {
        usersCollection
            .get()
            .addOnSuccessListener { documents ->
                val userList = mutableListOf<User>()
                for (doc in documents) {
                    val user = doc.toObject(User::class.java)
                    userList.add(user)
                }
                callback(userList)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // Find users by role
    fun findByRole(role: UserRole, callback: (List<User>) -> Unit) {
        usersCollection
            .whereEqualTo("role", role.name)
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.map { it.toObject(User::class.java) }
                callback(users)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // Save new user
    fun save(user: User, callback: (Boolean) -> Unit) {
        val id = usersCollection.document().id
        val newUser = user.copy(id = id)

        usersCollection.document(id)
            .set(newUser)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Update user
    fun update(user: User, callback: (Boolean) -> Unit) {
        usersCollection.document(user.id)
            .set(user)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Delete user
    fun deleteById(id: String, callback: (Boolean) -> Unit) {
        usersCollection.document(id)
            .delete()
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Check if email exists
    fun existsByEmail(email: String, callback: (Boolean) -> Unit) {
        usersCollection
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                callback(!documents.isEmpty)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}