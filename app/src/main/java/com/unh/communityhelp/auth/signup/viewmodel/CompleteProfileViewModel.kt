package com.unh.communityhelp.auth.signup.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

enum class OnboardingStep { GENERAL_INFO, QNA }

class CompleteProfileViewModel : ViewModel() {
    var fullName by mutableStateOf("")
    var username by mutableStateOf("")
    var phoneNumber by mutableStateOf("")

    val expertiseList = mutableStateListOf<String>()
    var currentStep by mutableStateOf(OnboardingStep.GENERAL_INFO)

    private val db by lazy { Firebase.firestore }

    fun moveToNextStep() { currentStep = OnboardingStep.QNA }

    fun submitProfile(onComplete: () -> Unit) {
        onComplete()
    }
}