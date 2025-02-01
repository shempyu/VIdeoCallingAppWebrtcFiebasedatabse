package com.example.webrtccalling.api

import com.example.webrtccalling.AccessToken
import com.example.webrtccalling.model.Notification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationInterface {
    @POST("/v1/projects/instagram-practice-833e0/messages:send")
    @Headers(
        "Content-Type:application/json",
        "Accept:application/json"
    )
    fun notification(@Body message: Notification,
                     @Header("Authorization") accessToken: String = "Bearer ${AccessToken.getAccessToken()}"

    ): Call<Notification>
}