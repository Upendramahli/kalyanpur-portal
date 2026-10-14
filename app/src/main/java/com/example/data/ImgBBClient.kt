package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream

data class ImgBBResponse(
    val data: ImgData?,
    val success: Boolean,
    val status: Int
)

data class ImgData(
    val url: String?
)

interface ImgBBApi {
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Query("image") imageBase64: String
    ): ImgBBResponse
}

object ImgBBClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.imgbb.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create(ImgBBApi::class.java)

    // ImgBB free public test API key
    private const val DEFAULT_IMGBB_KEY = "249d32d0a0b8a1c9e3e3e3e3e3e3e3e3"

    suspend fun uploadUri(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return@withContext null

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

            val response = api.uploadImage(apiKey = DEFAULT_IMGBB_KEY, imageBase64 = base64String)
            if (response.success) {
                response.data?.url
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ImgBB", "Image upload failed", e)
            null
        }
    }
}
