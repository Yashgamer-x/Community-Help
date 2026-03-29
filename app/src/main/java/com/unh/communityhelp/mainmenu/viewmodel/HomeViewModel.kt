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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.unh.communityhelp.mainmenu.model.HelpRequest
import com.unh.communityhelp.mainmenu.repository.LocationRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val db by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }
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
        val userCache = mutableMapOf<String, String>() // Cache: authorId -> username

        try {
            for (category in expertise) {
                val snapshot = db.collection("geolocation")
                    .document(city)
                    .collection("categories")
                    .document(category)
                    .collection("help_requests")
                    .get()
                    .await()

                // Inside HomeViewModel.kt (fetchTasksByLocationAndSkills)
                val tasks = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(HelpRequest::class.java)?.copy(
                        id = doc.id,
                        selfRef = doc.reference
                    )
                }
                allTasks.addAll(tasks)
            }

            // --- NEW: Fetch Usernames for each authorId ---
            val tasksWithNames = allTasks.map { task ->
                if (task.authorId.isNotEmpty()) {
                    // Check cache first, otherwise fetch from Firestore
                    val name = userCache[task.authorId] ?: try {
                        val userDoc = db.collection("users")
                            .document(task.authorId)
                            .get()
                            .await()
                        val fetchedName = userDoc.getString("username") ?: "Unknown User"
                        userCache[task.authorId] = fetchedName // Save to cache
                        fetchedName
                    } catch (e: Exception) {
                        "Unknown User"
                    }
                    task.copy(authorName = name)
                } else {
                    task.copy(authorName = "Anonymous")
                }
            }

            helpRequests = tasksWithNames.sortedByDescending { it.createdAt }

        } catch (e: Exception) {
            Log.e("Firestore", "Fetch Error: ${e.message}")
        } finally {
            isLoading = false
        }
    }


    fun acceptTask(request: HelpRequest, onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        // Use the self-contained reference
        val taskRef = request.selfRef ?: return

        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .update("acceptedTasks", FieldValue.arrayUnion(taskRef))
                    .await()

                onSuccess()
            } catch (e: Exception) {
                Log.e("HomeVM", "Accept Failed: ${e.message}")
            }
        }
    }
}