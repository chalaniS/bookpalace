package com.bookpalace.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bookpalace.app.R
import com.bookpalace.app.model.User
import com.bookpalace.app.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
                                startActivity(Intent(this, DashboardActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }

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
