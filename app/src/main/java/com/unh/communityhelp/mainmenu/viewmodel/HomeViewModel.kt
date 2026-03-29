package com.unh.communityhelp.mainmenu.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.unh.communityhelp.mainmenu.model.HelpRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val db = Firebase.firestore

    var helpRequests by mutableStateOf<List<HelpRequest>>(emptyList())
    var isLoading by mutableStateOf(false)

    // These would typically come from a User Profile or State
    private val currentUserCity = "West Haven"
    private val currentUserExpertise = listOf("General", "Cleaning", "Labor")

    init {
        fetchCategorizedTasks()
    }

    fun fetchCategorizedTasks() {
        viewModelScope.launch {
            isLoading = true
            val results = mutableListOf<HelpRequest>()

            try {
                // We iterate through categories based on user's expertise
                for (category in currentUserExpertise) {
                    val snapshot = db.collection("geolocation")
                        .document(currentUserCity)
                        .collection("categories")
                        .document(category)
                        .collection("help_requests")
                        .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .get()
                        .await()

                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(HelpRequest::class.java)?.copy(id = doc.id)
                    }
                    results.addAll(items)
                }

                // Sort the combined list by date (newest first)
                helpRequests = results.sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching tasks: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}