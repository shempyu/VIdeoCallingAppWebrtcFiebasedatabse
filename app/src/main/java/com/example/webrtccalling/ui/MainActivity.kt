package com.example.webrtccalling.ui

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.webrtccalling.adapters.MainRecyclerViewAdapter
import com.example.webrtccalling.databinding.ActivityMainBinding
import com.example.webrtccalling.repository.MainRepository
import com.example.webrtccalling.service.MainService
import com.example.webrtccalling.service.MainServiceRepository
import com.example.webrtccalling.utils.DataModel
import com.example.webrtccalling.utils.DataModelType
import com.example.webrtccalling.utils.getCameraAndMicPermission
import com.example.webrtccalling.utils.getCameraMicAndNotificationPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainRecyclerViewAdapter.Listener, MainService.Listener {
    private val TAG = "MainActivity"

    private lateinit var views: ActivityMainBinding
    private var username: String? = null

    @Inject
    lateinit var mainRepository: MainRepository
    @Inject
    lateinit var mainServiceRepository: MainServiceRepository
    private var mainAdapter: MainRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = ActivityMainBinding.inflate(layoutInflater)
        setContentView(views.root)
        views.logOutButton.setOnClickListener {
            logout()
        }

        init()

        // Allow strict mode for network calls (for simplicity in this demo)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    private fun init() {
        val auth = FirebaseAuth.getInstance()
        username = auth.currentUser?.uid // Set username as currentUserId

        if (username == null){
            startActivity(Intent(this,LoginActivity2::class.java))
            finish() // Ensure MainActivity doesn't continue running
            return
        }
        //1. observe other users status
        subscribeObservers()
        //2. start foreground service to listen negotiations and calls.
        startMyService()
    }

    private fun subscribeObservers() {
        setupRecyclerView()
        MainService.listener = this
        mainRepository.observeUsersStatus {
            Log.d(TAG, "subscribeObservers: $it")
            mainAdapter?.updateList(it, username!!)
        }
    }

    private fun setupRecyclerView() {
        mainAdapter = MainRecyclerViewAdapter(this)
        val layoutManager = LinearLayoutManager(this)
        views.mainRecyclerView.apply {
            setLayoutManager(layoutManager)
            adapter = mainAdapter
        }
    }

    private fun startMyService() {
        mainServiceRepository.startService(username!!)
    }


    private fun logout() {
        val userId = FirebaseAuth.getInstance().uid ?: return

        FirebaseDatabase.getInstance().getReference("User")
            .child(userId)
            .child("status")
            .setValue("OFFLINE")
            .addOnSuccessListener {
                FirebaseMessaging.getInstance().deleteToken()
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().getReference("User")
                            .child(userId)
                            .child("token")
                            .removeValue()
                            .addOnSuccessListener {
                                FirebaseAuth.getInstance().signOut()
                                startActivity(Intent(this, SignUpActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to remove token.", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete token.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update status to offline.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onVideoCallClicked(userId: String) {
        //check if permission of mic and camera is taken
        getCameraAndMicPermission {
            mainRepository.sendConnectionRequest(userId, true) {
                if (it){
                    //we have to start video call
                    //we wanna create an intent to move to call activity
                    startActivity(Intent(this,CallActivity::class.java).apply {
                        putExtra("target",userId)
                        putExtra("isVideoCall",true)
                        putExtra("isCaller",true)
                    })

                }
            }

        }
    }

    override fun onAudioCallClicked(userId: String) {
        getCameraAndMicPermission {
            mainRepository.sendConnectionRequest(userId, false) {
                if (it){
                    //we have to start audio call
                    //we wanna create an intent to move to call activity
                    startActivity(Intent(this,CallActivity::class.java).apply {
                        putExtra("target",userId)
                        putExtra("isVideoCall",false)
                        putExtra("isCaller",true)
                    })
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mainServiceRepository.stopService()
    }

    override fun onCallReceived(model: DataModel) {
        // Handle incoming call by starting CallRingingActivity
        val isVideoCall = model.type == DataModelType.StartVideoCall

        // Launch CallRingingActivity
        val intent = Intent(this, CallRingingActivity::class.java).apply {
            putExtra("sender", model.sender)
            putExtra("isVideoCall", isVideoCall)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }


}