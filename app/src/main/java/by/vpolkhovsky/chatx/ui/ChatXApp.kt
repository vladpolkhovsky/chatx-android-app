package by.vpolkhovsky.chatx.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import by.vpolkhovsky.chatx.core.NotificationController
import by.vpolkhovsky.chatx.ui.navidation.ChatXNavHost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatXApp(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(scope) {
        scope.launch {
            NotificationController.notificationFlow.collect {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        ChatXNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}