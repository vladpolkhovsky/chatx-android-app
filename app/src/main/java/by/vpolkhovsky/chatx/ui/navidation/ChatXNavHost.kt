package by.vpolkhovsky.chatx.ui.navidation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import by.vpolkhovsky.chatx.ui.chat.ChatScreen
import by.vpolkhovsky.chatx.ui.chat.ChatSelectorScreen
import by.vpolkhovsky.chatx.ui.login.LoginScreen

@Composable
fun ChatXNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = LoginDestination
    ) {
        composable<LoginDestination> {
            LoginScreen(
                onLoginToProfileId = { profileId ->
                    navController.navigate(ChatSelectionDestination(profileId))
                }
            )
        }
        composable<ChatSelectionDestination> {
            ChatSelectorScreen(
                onBackToLogin = {
                    navController.popBackStack(LoginDestination, false)
                },
                onChatSelected = { profileId, chatId ->
                    navController.navigate(ChatDestination(profileId, chatId))
                },
                onChangeSession = { profileId ->
                    navController.navigate(ChatSelectionDestination(profileId))
                }
            )
        }
        composable<ChatDestination> {
            ChatScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}