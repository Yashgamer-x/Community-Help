package com.unh.communityhelp.mainmenu.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.unh.communityhelp.mainmenu.model.HelpRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyTasksViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    var createdTasks by mutableStateOf<List<HelpRequest>>(emptyList())
    var acceptedTasks by mutableStateOf<List<HelpRequest>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun fetchUserTasks() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Get the User Document
                val userDoc = db.collection("users").document(uid).get().await()

                // 2. Extract arrays of References
                val createdRefs = userDoc.get("createdTasks") as? List<DocumentReference> ?: emptyList()
                val acceptedRefs = userDoc.get("acceptedTasks") as? List<DocumentReference> ?: emptyList()

                // 3. Run both fetch operations in parallel
                // Use coroutineScope to allow the use of 'async'
                coroutineScope {
                    val createdDeferred = async { fetchTasksFromRefs(createdRefs) }
                    val acceptedDeferred = async { fetchTasksFromRefs(acceptedRefs) }

                    createdTasks = createdDeferred.await()
                    acceptedTasks = acceptedDeferred.await()
                }

            } catch (e: Exception) {
                Log.e("MyTasksVM", "Error fetching tasks: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Fix: Wrapped in coroutineScope to provide a receiver for 'async'
     */
    private suspend fun fetchTasksFromRefs(refs: List<DocumentReference>): List<HelpRequest> = coroutineScope {
        refs.map { ref ->
            async {
                try {
                    val snapshot = ref.get().await()
                    // Convert snapshot to HelpRequest and inject the ID
                    snapshot.toObject(HelpRequest::class.java)?.copy(id = snapshot.id)
                } catch (e: Exception) {
                    Log.e("MyTasksVM", "Failed to fetch single ref: ${e.message}")
                    null
                }
            }
        }.awaitAll().filterNotNull().sortedByDescending { it.createdAt }
    }
}