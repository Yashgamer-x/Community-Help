package com.unh.communityhelp.mainmenu.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.unh.communityhelp.mainmenu.model.HelpRequest
import com.unh.communityhelp.mainmenu.repository.LocationRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    var helpRequests by mutableStateOf<List<HelpRequest>>(emptyList())
    var isLoading by mutableStateOf(false)
    var currentCity by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * Entry point: First gets User Profile, then triggers Geolocation
     */
    fun refreshHomeData(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        val locationRepo = LocationRepository(context, fusedLocationClient)

        viewModelScope.launch {
            isLoading = true
            val userId = auth.currentUser?.uid ?: return@launch

            try {
                // Step 1: Wait for the City Name
                val detectedCity = locationRepo.getCurrentCity()
                currentCity = detectedCity

                // Step 2: Get Interests
                val userDoc = db.collection("users").document(userId).get().await()
                val expertiseList = userDoc.get("expertiseList") as? List<String> ?: emptyList()

                // Step 3: Query Firestore
                if (detectedCity != "Unknown" && expertiseList.isNotEmpty()) {
                    fetchTasksByLocationAndSkills(detectedCity, expertiseList)
                } else {
                    isLoading = false
                    if (expertiseList.isEmpty()) errorMessage = "Please add skills to your profile."
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.message
            }
        }
    }

    private suspend fun fetchTasksByLocationAndSkills(city: String, expertise: List<String>) {
        val allTasks = mutableListOf<HelpRequest>()
        try {
            for (category in expertise) {
                val snapshot = db.collection("geolocation")
                    .document(city)
                    .collection("categories")
                    .document(category)
                    .collection("help_requests")
                    .get()
                    .await()

                val tasks = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(HelpRequest::class.java)?.copy(id = doc.id)
                }
                allTasks.addAll(tasks)
            }
            // Final Sort: Newest requests across all matched categories first
            helpRequests = allTasks.sortedByDescending { it.createdAt }
            Log.d("Firestore", "Fetch Success: ${helpRequests} tasks found")
        } catch (e: Exception) {
            Log.e("Firestore", "Fetch Error: ${e.message}")
        } finally {
            isLoading = false
        }
    }
}