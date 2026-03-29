package com.unh.communityhelp.mainmenu.viewmodel

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.unh.communityhelp.mainmenu.api.SpamApi
import com.unh.communityhelp.mainmenu.api.SpamRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class CreateHelpViewModel : ViewModel() {
    var capturedImage by mutableStateOf<Bitmap?>(null)
    var locationAddress by mutableStateOf("")
    var cityName by mutableStateOf("")
    var isFetchingLocation by mutableStateOf(false)
    var isSubmitting by mutableStateOf(false)
    var statusMessage by mutableStateOf("")

    // Retrofit Setup for Spam API
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://spam-detection-api-lyart.vercel.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val spamApi = retrofit.create(SpamApi::class.java)

    fun createHelpRequest(
        image: Bitmap?,
        title: String,
        description: String,
        location: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting = true
            statusMessage = "Analyzing content..."

            try {
                // 1. Call Spam Detection API (Only title and description as per docs)
                val response = spamApi.checkSpam(SpamRequest(title, description))

                Log.d("SpamCheck", "API Response: ${response.body()}")

                if (response.isSuccessful) {
                    val apiStatus = response.body()?.status?.lowercase()

                    when (apiStatus) {
                        "good" -> {
                            Log.d("SpamCheck","Verified! Posting to community...")
                            statusMessage = "Verified! Posting to community..."
                            saveToFirestore(image, title, description, location, onSuccess)
                        }
                        "bad" -> {
                            Log.d("SpamCheck","Post rejected: This looks like spam.")
                            statusMessage = "Post rejected: This looks like spam."
                            isSubmitting = false
                        }
                        else -> {
                            Log.d("SpamCheck","Unexpected response from server.")
                            statusMessage = "Unexpected response from server."
                            isSubmitting = false
                        }
                    }
                } else {
                    statusMessage = "Server error (${response.code()}). Try again later."
                    isSubmitting = false
                }
            } catch (e: Exception) {
                Log.e("SpamCheck", "Error: ${e.message}")
                statusMessage = "Check your connection and try again."
                isSubmitting = false
            }
        }
    }

    private suspend fun saveToFirestore(
        bitmap: Bitmap?,
        title: String,
        description: String,
        location: String,
        onSuccess: () -> Unit
    ) {
        val db = Firebase.firestore
        val userId = Firebase.auth.currentUser?.uid ?: "Anonymous"

        // Extract the City Name from the full location string (e.g., "West Haven, CT" -> "West Haven")
        val cityName = location.split(",").first().trim()

        // Determine Category (You can pass this as a param, or use 'title' as a placeholder)
        val categoryName = "General" // Or use logic to determine category from title

        // Prepare Image String
        val imageString = bitmap?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } ?: ""

        // Prepare the Data Object
        val requestData = hashMapOf(
            "authorId" to userId,
            "title" to title,
            "description" to description,
            "location" to location,
            "image" to imageString,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            // --- Firestore Hierarchy Construction ---
            // In Firestore, creating a document in a sub-collection automatically
            // creates the parent path if it doesn't exist.

            db.collection("geolocation")
                .document(cityName)
                .collection("categories")
                .document(categoryName)
                .collection("help_requests")
                .add(requestData)
                .await()

            isSubmitting = false
            statusMessage = "Post successful in $cityName!"
            onSuccess()
        } catch (e: Exception) {
            statusMessage = "Firestore Error: ${e.message}"
            isSubmitting = false
        }
    }

    fun clearImage() { capturedImage = null }
}