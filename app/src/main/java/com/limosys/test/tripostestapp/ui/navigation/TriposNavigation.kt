package com.limosys.test.tripostestapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        composable(route = AppRoutes.SALES_SCREEN.name + "/{identifier}", arguments = listOf(
            navArgument("identifier") {
                type = NavType.StringType
            }
        )) {
//            val identifier = it.arguments?.getString("identifier")
            val initializationViewModel: InitializationViewModel = hiltViewModel(remember(it) {
                navController.getBackStackEntry(AppRoutes.INITIALIZATION_SCREEN.name)
            })
            val salesViewModel: SalesViewModel = hiltViewModel()
            val state: SalesState = salesViewModel.salesState.collectAsState().value
            val debugState: DebugState = salesViewModel.debugState.collectAsState().value
            val initializationState = initializationViewModel.initializationState.collectAsState().value
            SalesScreen(navController = navController, state, salesViewModel::handleEvent, debugState, initializationState)
        }
    }
}