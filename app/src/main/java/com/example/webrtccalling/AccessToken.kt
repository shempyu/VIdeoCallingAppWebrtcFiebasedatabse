package com.example.webrtccalling

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AccessToken {
    private val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"
    fun getAccessToken(): String?{
        try{
            val jsonString = "PASTE YOUR PRIVATE KEY FROM SERVICE ACCOUNT ( JUST COPY AND PASTE THAT AFTER GENERATING) "
            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredential = GoogleCredentials.fromStream(stream)
                .createScoped(arrayListOf(firebaseMessagingScope))
            googleCredential.refresh()

            return googleCredential.accessToken.tokenValue
        }catch (e: IOException){
            return null
        }
    }
}
