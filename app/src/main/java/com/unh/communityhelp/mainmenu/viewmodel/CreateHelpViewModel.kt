package com.unh.communityhelp.mainmenu.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CreateHelpViewModel: ViewModel() {
    // Stores the captured image
    var capturedImage by mutableStateOf<Bitmap?>(null)

    // Location States
    var locationAddress by mutableStateOf("")
    var isFetchingLocation by mutableStateOf(false)
    var isSubmitting by mutableStateOf(false)

    // Reset function if they want to retake it
    fun clearImage() {
        capturedImage = null
    }
}