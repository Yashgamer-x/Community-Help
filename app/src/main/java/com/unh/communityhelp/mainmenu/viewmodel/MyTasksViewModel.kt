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
     * Fetches actual HelpRequest objects from a list of Firestore References
     * AND fetches the author's username for each task.
     */
    private suspend fun fetchTasksFromRefs(refs: List<DocumentReference>): List<HelpRequest> = coroutineScope {
        // 1. Fetch all Task Snapshots in parallel
        val taskSnapshots = refs.map { ref ->
            async {
                try { ref.get().await() } catch (e: Exception) { null }
            }
        }.awaitAll().filterNotNull()

        // 2. Identify unique author IDs to avoid redundant fetches
        val authorCache = mutableMapOf<String, String>()

        // 3. Process each task and resolve the username
        taskSnapshots.map { snapshot ->
            async {
                val task = snapshot.toObject(HelpRequest::class.java)?.copy(id = snapshot.id)

                if (task != null && task.authorId.isNotEmpty()) {
                    // Check if we already fetched this username in this batch
                    val username = authorCache[task.authorId] ?: try {
                        val userDoc = db.collection("users")
                            .document(task.authorId)
                            .get()
                            .await()
                        val name = userDoc.getString("username") ?: "Unknown User"
                        authorCache[task.authorId] = name
                        name
                    } catch (e: Exception) {
                        "Unknown User"
                    }

                    // Map the name back to the HelpRequest
                    task.copy(authorName = username)
                } else {
                    task
                }
            }
        }.awaitAll().filterNotNull().sortedByDescending { it.createdAt }
    }
}