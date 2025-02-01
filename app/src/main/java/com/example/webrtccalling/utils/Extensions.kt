package com.example.webrtccalling.utils

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX

fun AppCompatActivity.getCameraAndMicPermission(success: () -> Unit) {
    PermissionX.init(this)
        .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
        .request { allGranted, _, _ ->
            if (allGranted) {
                success()
            } else {
                Toast.makeText(this, "Camera and mic permission are required", Toast.LENGTH_SHORT).show()
            }
        }
}

fun AppCompatActivity.getNotificationPermission(success: () -> Unit) {
    PermissionX.init(this)
        .permissions(android.Manifest.permission.POST_NOTIFICATIONS)
        .request { allGranted, _, _ ->
            if (allGranted) {
                success()
            } else {
                Toast.makeText(this, "Notification permission is required", Toast.LENGTH_SHORT).show()
            }
        }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun AppCompatActivity.getCameraMicAndNotificationPermission(success: () -> Unit) {
    PermissionX.init(this)
        .permissions(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        .request { allGranted, _, _ ->
            if (allGranted) {
                success()
            } else {
                Toast.makeText(this, "Camera, mic, and notification permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
}

fun Int.convertToHumanTime(): String {
    val seconds = this % 60
    val minutes = this / 60
    val secondsString = if (seconds < 10) "0$seconds" else "$seconds"
    val minutesString = if (minutes < 10) "0$minutes" else "$minutes"
    return "$minutesString:$secondsString"
}
