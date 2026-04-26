package com.bookpalace.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bookpalace.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)

        loginBtn.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginBtn.isEnabled = false

            auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        updateFcmToken()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        loginBtn.isEnabled = true
                        handleLoginError(task.exception)
                    }
                }
        }
    }

    private fun handleLoginError(exception: Exception?) {
        val message = when (exception) {
            is FirebaseAuthInvalidUserException -> "No account found with this email."
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
            else -> "Login Failed: ${exception?.localizedMessage ?: "Unknown error"}"
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
                    Log.d("FCM", "Token updated on login")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error updating token", e)
                }
        }
    }
}
