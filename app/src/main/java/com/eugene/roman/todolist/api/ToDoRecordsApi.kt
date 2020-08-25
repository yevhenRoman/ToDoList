package com.eugene.roman.todolist.api

import android.util.Log
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ToDoRecordsApi {
    @GET("todos")
    suspend fun getToDoRecords(@Query("userId") userId: Long = 1): List<ToDoCloudTask>

    data class ToDoCloudTask(
        @SerializedName("userId")
        val userId: Long,
        @SerializedName("id")
        val id: Long,
        @SerializedName("title")
        val title: String,
        @SerializedName("completed")
        val isDone: Boolean
    )

    companion object {
        private const val BASE_URL = "http://jsonplaceholder.typicode.com"

        fun create(): ToDoRecordsApi {
            val logger = HttpLoggingInterceptor(logger = object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d("API", message)
                }
            })
            logger.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ToDoRecordsApi::class.java)
        }
    }
}