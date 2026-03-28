package com.unh.communityhelp.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unh.communityhelp.auth.scaffold.AuthScaffold
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@Composable
fun LoginView(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    AuthScaffold(title = "Welcome Back") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login to your account",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Your Input Fields (Email/Password) would go here
            OutlinedTextField(value = "", onValueChange = {}, label = { Text("Email") })

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginSuccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            TextButton(onClick = onNavigateToSignUp) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CommunityHelpTheme {
        LoginView(
            onNavigateToSignUp = {},
            onLoginSuccess = {}
        )
    }
}