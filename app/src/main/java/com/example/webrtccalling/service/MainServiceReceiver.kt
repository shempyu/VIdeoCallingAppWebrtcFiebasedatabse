package com.example.webrtccalling.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.webrtccalling.ui.CloseActivity

class MainServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_EXIT") {
            context?.let {
                val serviceRepository = MainServiceRepository(it)  // Pass context to MainServiceRepository
                serviceRepository.stopService()
                val closeIntent = Intent(it, CloseActivity::class.java)
                closeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                it.startActivity(closeIntent)
            }
        }
    }
}
