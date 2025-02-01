package com.example.webrtccalling.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.webrtccalling.databinding.ActivityCallRingingBinding
import com.example.webrtccalling.utils.getCameraAndMicPermission
import com.google.firebase.database.FirebaseDatabase

class CallRingingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallRingingBinding
    private var sender: String? = null
    private var isVideoCall: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sender = intent.getStringExtra("sender")
        isVideoCall = intent.getBooleanExtra("isVideoCall", false)

        setupUI()
    }

    private fun setupUI() {
        getUserName(sender!!) { sendername ->
            binding.incomingCallTitle.text = "$sendername is ${if (isVideoCall) "Video" else "Audio"} Calling you"
        }

        binding.acceptButton.setOnClickListener {
            getCameraAndMicPermission {
                // Start the CallActivity
                startActivity(Intent(this, CallActivity::class.java).apply {
                    putExtra("target", sender)
                    putExtra("isVideoCall", isVideoCall)
                    putExtra("isCaller", false)
                })
                finish()
                // Finish the CallRingingActivity
            }
        }

        binding.declineButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))

        }
    }

    private fun getUserName(userId: String, onResult: (String) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference().child("User").child(userId).child("name")

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val sendername = dataSnapshot.getValue(String::class.java) // Assuming the value is a String
            if (sendername != null) {
                // Successfully retrieved sender's name
                onResult(sendername) // Pass the result back via the callback
            } else {
                // If sender name is not found, use their UID
                onResult(userId)
            }
        }.addOnFailureListener { exception ->
            // If failed to retrieve sender name, use their UID
            Toast.makeText(this, "Failed to retrieve sender's name: ${exception.message}", Toast.LENGTH_SHORT).show()
            onResult(userId) // Return the userId in case of failure
        }
    }


}
