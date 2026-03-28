package com.unh.communityhelp.auth.signup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.unh.communityhelp.ui.theme.CommunityHelpTheme


@Composable
fun SignupView(
    modifier: Modifier = Modifier,
    onNavigateToLogin: ()-> Unit,
    onSignUpSuccess: ()-> Unit
){

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CommunityHelpTheme {
        SignupView(
            onNavigateToLogin = {},
            onSignUpSuccess = {}
        )
    }
}