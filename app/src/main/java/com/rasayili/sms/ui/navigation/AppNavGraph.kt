package com.rasayili.sms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rasayili.sms.ui.screens.ChatDetailScreen
import com.rasayili.sms.ui.screens.HomeScreen
import com.rasayili.sms.ui.screens.NewMessageScreen
import com.rasayili.sms.ui.viewmodel.SmsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: SmsViewModel,
    onRequestPermissions: () -> Unit,
    onRequestDefaultRole: () -> Unit
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToThread = { threadId, address -> navController.navigate("chat/$threadId/$address") },
                onNavigateToNewMessage = { navController.navigate("newMessage") },
                onRequestDefaultRole = onRequestDefaultRole
            )
        }
        composable(
            route = "chat/{threadId}/{address}",
            arguments = listOf(
                navArgument("threadId") { type = NavType.LongType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: 0L
            val address = backStackEntry.arguments?.getString("address") ?: ""
            ChatDetailScreen(threadId = threadId, address = address, viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
        }
        composable("newMessage") {
            NewMessageScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() }, onMessageSent = { threadId, address ->
                navController.popBackStack()
                navController.navigate("chat/$threadId/$address")
            })
        }
    }
}