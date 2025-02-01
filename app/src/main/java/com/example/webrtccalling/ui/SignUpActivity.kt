package com.example.webrtccalling.ui

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.webrtccalling.databinding.ActivitySignUpBinding
import com.example.webrtccalling.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val text = "<font color=#FF000000>Already have an account?</font> <font color=#1E88E5>Login</font>"
        binding.AlreadyLogin.setText(Html.fromHtml(text))

        binding.RegisterBtn.setOnClickListener {
            if (binding.name.editText?.text.toString().isEmpty() ||
                binding.email.editText?.text.toString().isEmpty() ||
                binding.password.editText?.text.toString().isEmpty()) {
                Toast.makeText(this, "Please fill the details", Toast.LENGTH_LONG).show()
            } else {
                // Register the user
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.email.editText?.text.toString(),
                    binding.password.editText?.text.toString()
                ).addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        // Fetch FCM Token after user is created
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result

                                // Create User object with token
                                user = User(
                                    name = binding.name.editText?.text.toString(),
                                    email = binding.email.editText?.text.toString(),
                                    password = binding.password.editText?.text.toString(),
                                    token = token, // Save the FCM token here
                                    status = "ONLINE",
                                    uid = FirebaseAuth.getInstance().currentUser!!.uid
                                )

                                // Save user data to Firebase Database
                                FirebaseDatabase.getInstance().reference
                                    .child("User")
                                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .setValue(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registration successful", Toast.LENGTH_LONG).show()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }

                            } else {
                                Toast.makeText(this, "Failed to get FCM token", Toast.LENGTH_LONG).show()
                            }
                        }

                    } else {
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.AlreadyLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity2::class.java))
            finish()
        }
    }
}