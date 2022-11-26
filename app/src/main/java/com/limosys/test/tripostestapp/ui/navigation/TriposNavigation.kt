package com.limosys.test.tripostestapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.limosys.test.tripostestapp.ui.routes.AppRoutes
import com.limosys.test.tripostestapp.ui.screens.InitializationScreen
import com.limosys.test.tripostestapp.ui.screens.SalesScreen

@Composable
fun TriposNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoutes.INITIALIZATION_SCREEN.name) {
        composable(route = AppRoutes.INITIALIZATION_SCREEN.name) {
            InitializationScreen(navController = navController)
        }
        composable(route = AppRoutes.SALES_SCREEN.name) {
            SalesScreen(navController = navController)
        }
    }
}