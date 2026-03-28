package com.unh.communityhelp.auth.signup.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unh.communityhelp.auth.scaffold.AuthScaffold
import com.unh.communityhelp.auth.signup.viewmodel.SignupState
import com.unh.communityhelp.auth.signup.viewmodel.SignupViewModel
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@Composable
fun SignupView(
    modifier: Modifier = Modifier,
    viewModel: SignupViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val signupState by viewModel.signupState.collectAsState()

    SignupContent(
        modifier = modifier,
        signupState = signupState,
        onSignUp = { email, password, fullName ->
            viewModel.signUp(email, password, fullName)
        },
        onNavigateToLogin = onNavigateToLogin,
        onSignUpSuccess = onSignUpSuccess
    )
}

@Composable
fun SignupContent(
    modifier: Modifier = Modifier,
    signupState: SignupState,
    onSignUp: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Trigger navigation on success
    LaunchedEffect(signupState) {
        if (signupState is SignupState.Success) {
            onSignUpSuccess()
        }
    }

    AuthScaffold(title = "Join Community Help") { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Create your account", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = signupState !is SignupState.Loading
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = signupState !is SignupState.Loading
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = signupState !is SignupState.Loading
            )

            // Show Error Message if it exists
            if (signupState is SignupState.Error) {
                Text(
                    text = signupState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSignUp(email, password, fullName) },
                modifier = Modifier.fillMaxWidth(),
                enabled = signupState !is SignupState.Loading &&
                        email.isNotBlank() && password.isNotBlank() && fullName.isNotBlank()
            ) {
                if (signupState is SignupState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign Up")
                }
            }

            TextButton(onClick = onNavigateToLogin, enabled = signupState !is SignupState.Loading) {
                Text("Already have an account? Log In")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupPreview() {
    CommunityHelpTheme {
        SignupContent(
            signupState = SignupState.Idle,
            onSignUp = { _, _, _ -> },
            onNavigateToLogin = {},
            onSignUpSuccess = {}
        )
    }
}