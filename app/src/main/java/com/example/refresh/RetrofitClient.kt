package com.example.refresh.network

import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5000/"  // Use 10.0.2.2 for localhost on emulator

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)


    }

    // Cloudinary Client for Image Uploads
    object CloudinaryClient {
        private const val BASE_URL = "https://api.cloudinary.com/v1_1/dlnq8eaed/image/upload"

        val api: CloudinaryService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CloudinaryService::class.java)
        }
    }

    interface CloudinaryService {
        @Multipart
        @POST
        suspend fun uploadImage(
            @Part file: MultipartBody.Part,
            @Part("upload_preset") preset: RequestBody
        ): UploadResponse
    }

    data class UploadResponse(
        val url: String // URL of the uploaded image from Cloudinary
    )
}
