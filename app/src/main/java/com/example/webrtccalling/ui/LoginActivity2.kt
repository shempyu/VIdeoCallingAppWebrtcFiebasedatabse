package com.example.webrtccalling.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.webrtccalling.databinding.ActivityLogin2Binding
import com.example.webrtccalling.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityLogin2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.Login.setOnClickListener {
            if (binding.LoginEmail.editText?.text.toString().equals("") or
                binding.LoginPassword.editText?.text.toString().equals("")
            ) {
                Toast.makeText(this, "please fill the details", Toast.LENGTH_LONG).show()

            } else {
                var user = User(
                    binding.LoginEmail.editText?.text.toString(),
                    binding.LoginPassword.editText?.text.toString()
                )
                Firebase.auth.signInWithEmailAndPassword(user.email!!, user.password!!)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            setStatus()
                            setToken()
                            Toast.makeText(this, "login successful", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()

                        } else {
                            Toast.makeText(this, "login failed", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
        binding.newAccount.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }
    private fun setToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            if(it.isNotEmpty()){
                FirebaseDatabase.getInstance().getReference("User")
                    .child(FirebaseAuth.getInstance().uid!!)
                    .child("token")
                    .setValue(it)
            }
        }
    }
    private fun setStatus(){
        FirebaseDatabase.getInstance().getReference("User")
            .child(FirebaseAuth.getInstance().uid!!)
            .child("status")
            .setValue("ONLINE")

    }
}