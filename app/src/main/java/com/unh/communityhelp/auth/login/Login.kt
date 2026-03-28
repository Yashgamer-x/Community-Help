package com.unh.communityhelp.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@Composable
fun LoginView(
    modifier: Modifier = Modifier,
    onNavigateToSignUp: ()-> Unit
){

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CommunityHelpTheme {
        LoginView(
            onNavigateToSignUp = {}
        )
    }
}