package com.example.webrtccalling.firebaseClient


import com.example.webrtccalling.utils.DataModel
import com.example.webrtccalling.utils.FirebaseFieldNames.LATEST_EVENT
import com.example.webrtccalling.utils.FirebaseFieldNames.STATUS
import com.example.webrtccalling.utils.MyEventListener
import com.example.webrtccalling.webrtc.utils.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor(
    private val dbRef:DatabaseReference,
    private val gson:Gson
) {

    private var currentUsername:String?=null

    fun setUsername() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (!currentUserId.isNullOrEmpty()) {
            this.currentUsername = currentUserId // Use Firebase UID as username
        }
    }




    fun observeUsersStatus(status: (List<Pair<String, String>>) -> Unit) {
        setUsername()
        dbRef.child("User").addValueEventListener(object : MyEventListener() {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.filter { it.key !=currentUsername }.map {
                    it.key!! to it.child(STATUS).value.toString()
                }
                status(list)
            }
        })
    }

    fun subscribeForLatestEvent(listener:Listener){
        try {
            setUsername()
            dbRef.child("User").child(currentUsername!!).child(LATEST_EVENT).addValueEventListener(
                object : MyEventListener() {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        super.onDataChange(snapshot)
                        val event = try {
                            gson.fromJson(snapshot.value.toString(), DataModel::class.java)
                        }catch (e:Exception){
                            e.printStackTrace()
                            null
                        }
                        event?.let {
                            listener.onLatestEventReceived(it)
                        }
                    }
                }
            )
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun sendMessageToOtherClient(message:DataModel, success:(Boolean) -> Unit){
        setUsername()
        val convertedMessage = gson.toJson(message.copy(sender = currentUsername))
        dbRef.child("User").child(message.target).child(LATEST_EVENT).setValue(convertedMessage)
            .addOnCompleteListener {
                success(true)
            }.addOnFailureListener {
                success(false)
            }
    }

    fun changeMyStatus(status: UserStatus) {
        setUsername()
        dbRef.child("User").child(currentUsername!!).child(STATUS).setValue(status.name)
    }

    fun clearLatestEvent() {
        setUsername()
        dbRef.child("User").child(currentUsername!!).child(LATEST_EVENT).setValue(null)
    }

    fun logOff(function:()->Unit) {
        setUsername()
        dbRef.child("User").child(currentUsername!!).child(STATUS).setValue(UserStatus.OFFLINE)
            .addOnCompleteListener { function() }
    }


    interface Listener {
        fun onLatestEventReceived(event:DataModel)
    }
}