package com.rasayili.sms.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rasayili.sms.ui.screens.ChatDetailScreen
import com.rasayili.sms.ui.screens.HomeScreen
import com.rasayili.sms.ui.screens.NewMessageScreen
import com.rasayili.sms.ui.screens.SettingsScreen
import com.rasayili.sms.ui.viewmodel.SmsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: SmsViewModel,
    onRequestPermissions: () -> Unit,
    onRequestDefaultRole: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToThread = { threadId, address ->
                    navController.navigate(
                        "chat/$threadId/${Uri.encode(address)}"
                    )
                },
                onNavigateToNewMessage = {
                    navController.navigate("newMessage")
                },
                onNavigateToContact = { number ->
                    navController.navigate(
                        "newMessage?recipient=${Uri.encode(number)}"
                    )
                },
                onRequestDefaultRole = onRequestDefaultRole,
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "chat/{threadId}/{address}",
            arguments = listOf(
                navArgument("threadId") {
                    type = NavType.LongType
                },
                navArgument("address") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val threadId =
                entry.arguments?.getLong("threadId") ?: 0L
            val address =
                Uri.decode(entry.arguments?.getString("address") ?: "")

            ChatDetailScreen(
                threadId = threadId,
                address = address,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "newMessage?recipient={recipient}",
            arguments = listOf(
                navArgument("recipient") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { entry ->
            val recipient =
                entry.arguments?.getString("recipient") ?: ""

            NewMessageScreen(
                viewModel = viewModel,
                initialRecipient = recipient,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onMessageSent = { threadId, address ->
                    navController.popBackStack()
                    navController.navigate(
                        "chat/$threadId/${Uri.encode(address)}"
                    )
                }
            )
        }
    }
}