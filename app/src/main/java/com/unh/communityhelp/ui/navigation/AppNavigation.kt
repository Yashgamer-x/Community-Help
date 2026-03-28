package com.unh.communityhelp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unh.communityhelp.auth.login.LoginView
import com.unh.communityhelp.auth.signup.SignupView

enum class AppScreen {
    Login,
    SignUp,
}

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreen.Login.name){
        authGraph(navController)
    }
}

fun NavGraphBuilder.authGraph(navController: NavHostController){
    composable (AppScreen.Login.name){
        LoginView(onNavigateToSignUp = {navController.navigate(AppScreen.SignUp.name)})
    }
    composable(AppScreen.SignUp.name){
        SignupView(
            onNavigateToLogin = {navController.navigate(AppScreen.Login.name)}
        )
    }
}