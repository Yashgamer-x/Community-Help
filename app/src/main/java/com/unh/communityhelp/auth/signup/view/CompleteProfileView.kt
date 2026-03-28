package com.unh.communityhelp.auth.signup.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unh.communityhelp.auth.scaffold.AuthScaffold
import com.unh.communityhelp.auth.signup.viewmodel.CompleteProfileViewModel
import com.unh.communityhelp.auth.signup.viewmodel.OnboardingStep
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@Composable
fun CompleteProfileView(
    viewModel: CompleteProfileViewModel = viewModel(),
    onProfileComplete: () -> Unit
) {
    AuthScaffold(
        title = if (viewModel.currentStep == OnboardingStep.GENERAL_INFO) "General Info" else "Expertise & QNA"
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            when (viewModel.currentStep) {
                OnboardingStep.GENERAL_INFO -> {
                    GeneralInfoPart(
                        viewModel = viewModel,
                        onNext = { viewModel.moveToNextStep() }
                    )
                }
                OnboardingStep.QNA -> {
                    ExpertiseQNAPart(
                        viewModel = viewModel,
                        onFinish = { viewModel.submitProfile(onProfileComplete) }
                    )
                }
            }
        }
    }
}

@Composable
fun GeneralInfoPart(viewModel: CompleteProfileViewModel, onNext: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = viewModel.fullName,
            onValueChange = { viewModel.fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.username,
            onValueChange = { viewModel.username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.phoneNumber,
            onValueChange = { viewModel.phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.username.isNotBlank() && viewModel.fullName.isNotBlank()
        ) {
            Text("Next: Expertise")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpertiseQNAPart(viewModel: CompleteProfileViewModel, onFinish: () -> Unit) {
    var skillInput by remember { mutableStateOf("") }

    Column {
        Text("What are your areas of expertise?", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            OutlinedTextField(
                value = skillInput,
                onValueChange = { skillInput = it },
                modifier = Modifier.weight(1f),
                label = { Text("Add Expertise (e.g. Electrician)") }
            )
            IconButton (onClick = {
                if (skillInput.isNotBlank()) {
                    viewModel.expertiseList.add(skillInput)
                    skillInput = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.expertiseList.forEach { skill ->
                InputChip(
                    selected = true,
                    onClick = { viewModel.expertiseList.remove(skill) },
                    label = { Text(skill) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.expertiseList.isNotEmpty()
        ) {
            Text("Finish Registration")
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Step 1: General Info")
@Composable
fun PreviewGeneralInfo() {
    CommunityHelpTheme {
        val mockViewModel = CompleteProfileViewModel().apply {
            fullName = "John Doe"
            username = "johndoe123"
            phoneNumber = "123-456-7890"
        }

        AuthScaffold(title = "General Info") { padding ->
            Box(modifier = Modifier.padding(padding).padding(24.dp)) {
                GeneralInfoPart(
                    viewModel = mockViewModel,
                    onNext = {}
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Step 2: Expertise & QNA")
@Composable
fun PreviewExpertiseQNA() {
    CommunityHelpTheme {
        val mockViewModel = CompleteProfileViewModel().apply {
            expertiseList.add("Plumbing")
            expertiseList.add("Electrician")
            expertiseList.add("Carpentry")
        }

        AuthScaffold(title = "Expertise & QNA") { padding ->
            Box(modifier = Modifier.padding(padding).padding(24.dp)) {
                ExpertiseQNAPart(
                    viewModel = mockViewModel,
                    onFinish = {}
                )
            }
        }
    }
}