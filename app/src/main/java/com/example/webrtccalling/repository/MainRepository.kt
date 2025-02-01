package com.example.webrtccalling.repository

import android.content.Intent
import android.util.Log
import com.example.webrtccalling.firebaseClient.FirebaseClient
import com.example.webrtccalling.utils.DataModel
import com.example.webrtccalling.utils.DataModelType
import com.example.webrtccalling.webrtc.MyPeerObserver
import com.example.webrtccalling.webrtc.WebRTCClient
import com.example.webrtccalling.webrtc.utils.UserStatus
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val firebaseClient: FirebaseClient,
    private val webRTCClient: WebRTCClient,
    private val gson: Gson
) : WebRTCClient.Listener {

    private var target: String? = null
    var listener: Listener? = null
    private var remoteView:SurfaceViewRenderer?=null
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference



    fun observeUsersStatus(status: (List<Pair<String, String>>) -> Unit) {
        firebaseClient.observeUsersStatus(status)
    }

    fun initFirebase() {
        firebaseClient.subscribeForLatestEvent(object : FirebaseClient.Listener {
            override fun onLatestEventReceived(event: DataModel) {
                listener?.onLatestEventReceived(event)
                when (event.type) {
                    DataModelType.Offer ->{
                        webRTCClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.OFFER,
                                event.data.toString()
                            )
                        )
                        webRTCClient.answer(target!!)
                    }
                    DataModelType.Answer ->{
                        webRTCClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.ANSWER,
                                event.data.toString()
                            )
                        )
                    }
                    DataModelType.IceCandidates ->{
                        val candidate: IceCandidate? = try {
                            gson.fromJson(event.data.toString(),IceCandidate::class.java)
                        }catch (e:Exception){
                            null
                        }
                        candidate?.let {
                            webRTCClient.addIceCandidateToPeer(it)
                        }
                    }
                    DataModelType.EndCall ->{
                        listener?.endCall()
                    }
                    else -> Unit
                }
            }

        })
    }

    fun sendConnectionRequest(target: String, isVideoCall: Boolean, success: (Boolean) -> Unit) {
        firebaseClient.sendMessageToOtherClient(
            DataModel(
                type = if (isVideoCall) DataModelType.StartVideoCall else DataModelType.StartAudioCall,
                target = target
            ), success
        )
    }

    fun setTarget(target: String) {
        this.target = target
    }

    interface Listener {
        fun onLatestEventReceived(data: DataModel)
        fun endCall()
    }

    fun initWebrtcClient(username: String) {
        webRTCClient.listener = this
        webRTCClient.initializeWebrtcClient(username, object : MyPeerObserver() {

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                try {
                    p0?.videoTracks?.get(0)?.addSink(remoteView)
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }

            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let {
                    webRTCClient.sendIceCandidate(target!!, it)
                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    // 1. change my status to in call
                    changeMyStatus(UserStatus.IN_CALL)
                    // 2. clear latest event inside my user section in firebase database
                    firebaseClient.clearLatestEvent()
                }
            }
        })
    }

    fun initLocalSurfaceView(view: SurfaceViewRenderer, isVideoCall: Boolean) {
        webRTCClient.initLocalSurfaceView(view, isVideoCall)
    }

    fun initRemoteSurfaceView(view: SurfaceViewRenderer) {
        webRTCClient.initRemoteSurfaceView(view)
        this.remoteView = view
    }

    fun startCall() {
        webRTCClient.call(target!!)
    }

    fun endCall() {
        webRTCClient.closeConnection()
        changeMyStatus(UserStatus.ONLINE)
    }

    fun sendEndCall() {
        onTransferEventToSocket(
            DataModel(
                type = DataModelType.EndCall,
                target = target!!
            )
        )
    }

    private fun changeMyStatus(status: UserStatus) {
        firebaseClient.changeMyStatus(status)
    }

    fun toggleAudio(shouldBeMuted: Boolean) {
        webRTCClient.toggleAudio(shouldBeMuted)
    }

    fun toggleVideo(shouldBeMuted: Boolean) {
        webRTCClient.toggleVideo(shouldBeMuted)
    }

    fun switchCamera() {
        webRTCClient.switchCamera()
    }

    override fun onTransferEventToSocket(data: DataModel) {
        firebaseClient.sendMessageToOtherClient(data) {}
    }

    fun setScreenCaptureIntent(screenPermissionIntent: Intent) {
        webRTCClient.setPermissionIntent(screenPermissionIntent)
    }

    fun toggleScreenShare(isStarting: Boolean) {
        if (isStarting){
            webRTCClient.startScreenCapturing()
        }else{
            webRTCClient.stopScreenCapturing()
        }
    }

    fun logOff(function: () -> Unit) = firebaseClient.logOff(function)



}