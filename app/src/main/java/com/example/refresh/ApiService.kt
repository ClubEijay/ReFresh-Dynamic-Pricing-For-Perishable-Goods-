package com.example.refresh.network

import com.example.refresh.CartItemRequest
import com.example.refresh.CartResponse
import com.example.refresh.Order
import com.example.refresh.ProductsDebugResponse
import com.example.refresh.model.LoginRequest
import com.example.refresh.model.LoginResponse
import com.example.refresh.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("/signup")
    suspend fun signUp(@Body user: User): Response<Map<String, String>>

    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/add-product")
    suspend fun addProduct(@Body product: Product): Response<Map<String, String>>

    @GET("/products/{email}")
    suspend fun getUserProducts(@Path(value = "email", encoded = true) email: String): Response<List<Product>>

    @GET("/products")
    suspend fun getAllProducts(@Query("email") email: String = ""): Response<List<Product>>

   // Alternative - use debug endpoint for all products
    @GET("/debug/products")
    suspend fun getAllProductsDebug(): Response<ProductsDebugResponse>
    @PUT("update-profile")
    suspend fun updateProfile(@Body updateData: Map<String, String>): Response<Map<String, Any>>

    @PUT("change-password")
    suspend fun changePassword(@Body passwordData: Map<String, String>): Response<Map<String, Any>>

    @PUT("/update-product/{productId}")
    suspend fun updateProduct(@Path("productId") productId: String, @Body updatedProduct: Product): Response<Map<String, String>>

    @DELETE("/delete-product/{productId}")
    suspend fun deleteProduct(@Path("productId") productId: String): Response<Map<String, String>>


    @POST("/add-to-cart")
    suspend fun addToCart(@Body cartItem: CartItemRequest): Response<Map<String, String>>

    @GET("/cart")
    suspend fun getCart(@Query("email") email: String): Response<CartResponse>

    @DELETE("/cart/{id}")
    suspend fun removeFromCart(@Path("id") cartItemId: String): Response<Map<String, String>>

    // Fixed endpoint to match server implementation
    @POST("/orders")
    suspend fun createOrder(@Body order: Order): Response<Map<String, String>>

}


