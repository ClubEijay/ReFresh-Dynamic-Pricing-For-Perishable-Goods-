package com.example.refresh.network

import com.example.refresh.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/signup")
    suspend fun signUp(@Body user: User): Response<Map<String, String>>

    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
