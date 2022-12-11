package com.limosys.test.tripostestapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.limosys.test.tripostestapp.ui.routes.AppRoutes
import com.limosys.test.tripostestapp.ui.screens.initialization.InitializationScreen
import com.limosys.test.tripostestapp.ui.screens.sales.SalesScreen
import com.limosys.test.tripostestapp.ui.screens.initialization.InitializationViewModel
import com.limosys.test.tripostestapp.ui.screens.sales.SalesViewModel
import com.limosys.test.tripostestapp.ui.screens.states.DebugState
import com.limosys.test.tripostestapp.ui.screens.states.InitializationState
import com.limosys.test.tripostestapp.ui.screens.states.SalesState

@Composable
fun TriposNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoutes.INITIALIZATION_SCREEN.name) {
        composable(route = AppRoutes.INITIALIZATION_SCREEN.name) {
            val viewModel: InitializationViewModel = hiltViewModel()
            val state: InitializationState = viewModel.initializationState.collectAsState().value
            InitializationScreen(navController = navController, state, viewModel::handleEvents, viewModel.showDetails)
        }
        composable(route = AppRoutes.SALES_SCREEN.name) {
            val viewModel: SalesViewModel = hiltViewModel()
            val state: SalesState = viewModel.salesState.collectAsState().value
            val debugState: DebugState = viewModel.debugState.collectAsState().value
            SalesScreen(navController = navController, state, viewModel::handleEvent, debugState)
        }
    }
}