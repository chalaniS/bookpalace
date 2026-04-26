package com.bookpalace.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bookpalace.app.R
import com.bookpalace.app.model.User
import com.bookpalace.app.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        val name = findViewById<EditText>(R.id.name)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val phone = findViewById<EditText>(R.id.phone)
        val address = findViewById<EditText>(R.id.address)
        val signupBtn = findViewById<Button>(R.id.signupBtn)

        signupBtn.setOnClickListener {

            val nameText = name.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val phoneText = phone.text.toString().trim()
            val addressText = address.text.toString().trim()

            if (nameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText.length < 6) {
                password.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            signupBtn.isEnabled = false // Prevent double clicks

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser!!.uid

                        val user = User(
                            id = userId,
                            name = nameText,
                            email = emailText,
                            phone = phoneText,
                            address = addressText,
                            role = UserRole.STUDENT,
                            registeredAt = System.currentTimeMillis().toString(),
                            isActive = true
                        )

                        database.child(userId).setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                updateFcmToken()
                                startActivity(Intent(this, DashboardActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                signupBtn.isEnabled = true
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        signupBtn.isEnabled = true
                        handleSignupError(task.exception)
                    }
                }
        }
    }

    private fun handleSignupError(exception: Exception?) {
        val message = when (exception) {
            is FirebaseAuthUserCollisionException -> {
                "This email is already registered. Please login or use another email."
            }
            is FirebaseAuthWeakPasswordException -> {
                "The password is too weak. Please use a stronger password."
            }
            is FirebaseAuthInvalidCredentialsException -> {
                "The email address is badly formatted."
            }
            else -> {
                Log.e("SignupError", "Error: ${exception?.message}", exception)
                "Signup Failed: ${exception?.localizedMessage ?: "Unknown error"}"
            }
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

            val tokenData = hashMapOf(
                "userId" to userId,
                "fcmToken" to token,
                "lastUpdated" to com.google.firebase.Timestamp.now()
            )

            FirebaseFirestore.getInstance().collection("user_tokens")
                .document(userId)
                .set(tokenData)
                .addOnSuccessListener {
                    Log.d("FCM", "Token updated on signup")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error updating token", e)
                }
        }
    }
}
