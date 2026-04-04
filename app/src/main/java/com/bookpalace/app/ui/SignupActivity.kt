package com.bookpalace.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bookpalace.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val userId = auth.currentUser!!.uid

                        val user = hashMapOf(
                            "id" to userId,
                            "name" to nameText,
                            "email" to emailText,
                            "phone" to phoneText,
                            "address" to addressText,
                            "registered_at" to LocalDateTime.now().toString(),
                            "is_active" to true
                        )

                        db.collection("users")
                            .document(userId)
                            .set(user)

                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this, WelcomePage::class.java))
                        finish()

                    } else {

                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthUserCollisionException) {

                            Toast.makeText(
                                this,
                                "This email is already registered. Please login.",
                                Toast.LENGTH_LONG
                            ).show()

                        } catch (e: Exception) {

                            Toast.makeText(
                                this,
                                "Signup Failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()

                        }

                    }

                }

        }
    }
}