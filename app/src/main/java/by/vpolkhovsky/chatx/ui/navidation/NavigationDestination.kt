package by.vpolkhovsky.chatx.ui.navidation

import kotlinx.serialization.Serializable

interface NavigationDestination {
    val route: String
}

@Serializable
object LoginDestination : NavigationDestination {
    override val route: String = "login"
}

@Serializable
class ChatSelectionDestination(val currentProfileId: Int) : NavigationDestination {
    override val route: String = "chat-selection"
}

@Serializable
class ChatDestination(val currentProfileId: Int, val chatId: Int) : NavigationDestination {
    override val route: String = "chat"
}