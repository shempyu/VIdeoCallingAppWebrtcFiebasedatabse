package com.example.webrtccalling.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.webrtccalling.R
import com.example.webrtccalling.databinding.ItemMainRecyclerViewBinding
import com.example.webrtccalling.model.Notification
import com.example.webrtccalling.model.NotificationData
import com.example.webrtccalling.api.NotificationAPI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainRecyclerViewAdapter(private val listener: Listener) :
    RecyclerView.Adapter<MainRecyclerViewAdapter.MainRecyclerViewHolder>() {

    private var usersList: List<Pair<String, String>> = emptyList()
    private var currentUser: String = ""
    private val nameCache = mutableMapOf<String, String>()

    fun updateList(list: List<Pair<String, String>>, currentUser: String) {
        this.usersList = list
        this.currentUser = currentUser
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainRecyclerViewHolder {
        val binding = ItemMainRecyclerViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MainRecyclerViewHolder(binding, listener, currentUser, nameCache)
    }

    override fun getItemCount(): Int = usersList.size

    override fun onBindViewHolder(holder: MainRecyclerViewHolder, position: Int) {
        val user = usersList[position]
        holder.bind(user)
    }

    interface Listener {
        fun onVideoCallClicked(userId: String)
        fun onAudioCallClicked(userId: String)
    }

    class MainRecyclerViewHolder(
        private val binding: ItemMainRecyclerViewBinding,
        private val listener: Listener,
        private val currentUser: String,
        private val nameCache: MutableMap<String, String>
    ) : RecyclerView.ViewHolder(binding.root) {

        private val context = binding.root.context
        private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("User")

        fun bind(user: Pair<String, String>) {
            val userId = user.first
            if (nameCache.containsKey(userId)) {
                binding.usernameTv.text = nameCache[userId] ?: userId
            } else {
                databaseRef.child(userId).child("name").get().addOnSuccessListener { snapshot ->
                    val name = snapshot.getValue(String::class.java)
                    nameCache[userId] = name ?: userId
                    binding.usernameTv.text = name ?: userId
                }.addOnFailureListener {
                    binding.usernameTv.text = userId
                }
            }
            updateStatus(user.second, userId)
        }

        private fun updateStatus(status: String, userId: String) {
            when (status) {
                "ONLINE" -> {
                    setStatusUI(
                        videoCallVisible = true,
                        audioCallVisible = true,
                        statusText = "Online",
                        statusColorRes = R.color.light_green,
                        userId = userId,
                        videoClick = { onVideoCall(userId) },
                        audioClick = { onAudioCall(userId) }
                    )
                }
                "OFFLINE" -> {
                    setStatusUI(
                        videoCallVisible = false,
                        audioCallVisible = false,
                        statusText = "Offline",
                        userId = userId,
                        statusColorRes = R.color.red
                    )
                }
                "IN_CALL" -> {
                    setStatusUI(
                        videoCallVisible = false,
                        audioCallVisible = false,
                        statusText = "In Call",
                        userId = userId,
                        statusColorRes = R.color.yellow
                    )
                }
            }
        }

        private fun setStatusUI(
            videoCallVisible: Boolean,
            audioCallVisible: Boolean,
            statusText: String,
            statusColorRes: Int,
            videoClick: (() -> Unit)? = null,
            audioClick: (() -> Unit)? = null,
            userId: String
        ) {
            binding.apply {
                videoCallBtn.isVisible = videoCallVisible
                audioCallBtn.isVisible = audioCallVisible

                videoCallBtn.setOnClickListener {
                    videoClick?.invoke()
                    sendCallNotification(userId, true) }
                audioCallBtn.setOnClickListener {
                    audioClick?.invoke()
                    sendCallNotification(userId, false) }

                statusTv.text = statusText
                statusTv.setTextColor(context.resources.getColor(statusColorRes, null))
            }
        }
        private fun onVideoCall(username: String) {
            listener.onVideoCallClicked(username)

        }

        private fun onAudioCall(username: String) {
            listener.onAudioCallClicked(username)

        }
        private fun sendCallNotification(target: String, isVideoCall: Boolean) {
            val fcmTokenRef = FirebaseDatabase.getInstance().getReference("User").child(target).child("token")
            fcmTokenRef.get().addOnSuccessListener { snapshot ->
                val targetToken = snapshot.getValue(String::class.java)
                if (!targetToken.isNullOrEmpty()) {
                    val senderUid = FirebaseAuth.getInstance().uid ?: return@addOnSuccessListener

                    val notificationData = NotificationData(
                        token = targetToken,
                        data = hashMapOf(
                            "type" to "incoming_call",
                            "sender" to senderUid,
                            "isVideoCall" to isVideoCall.toString()
                        )
                    )

                    val notification = Notification(message = notificationData)

                    NotificationAPI.sendNotification().notification(notification).enqueue(object : Callback<Notification> {
                        override fun onResponse(call: Call<Notification>, response: Response<Notification>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Notification sent to $target", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to send notification. ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Notification>, t: Throwable) {
                            Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "User's token is missing.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to fetch user's token.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
