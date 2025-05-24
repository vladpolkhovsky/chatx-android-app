package by.vpolkhovsky.chatx.core

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import by.vpolkhovsky.chatx.data.ChatService
import by.vpolkhovsky.chatx.data.UserProfileService
import by.vpolkhovsky.chatx.data.http.OkHttpProvider
import by.vpolkhovsky.chatx.data.persistence.ChatXAppDatabase
import by.vpolkhovsky.chatx.data.persistence.repository.ChatRepository
import by.vpolkhovsky.chatx.data.persistence.repository.MessageRepository
import by.vpolkhovsky.chatx.data.persistence.repository.OfflineChatRepository
import by.vpolkhovsky.chatx.data.persistence.repository.OfflineSessionRepository
import by.vpolkhovsky.chatx.data.persistence.repository.OnlineChatRepository
import by.vpolkhovsky.chatx.data.persistence.repository.OnlineSessionRepository
import by.vpolkhovsky.chatx.data.persistence.repository.SessionRepository
import by.vpolkhovsky.chatx.ui.chat.ChatScreenViewModel
import by.vpolkhovsky.chatx.ui.chat.ChatSelectorViewModel
import by.vpolkhovsky.chatx.ui.login.LoginScreenViewModel

class ChatXApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val offlineSessionRepository: SessionRepository
    val onlineSessionRepository: SessionRepository
    val offlineChatRepository: ChatRepository
    val onlineChatRepository: ChatRepository
    val okHttpProvider: OkHttpProvider
    val userProfileService: UserProfileService
    val chatService: ChatService
    val messageRepository: MessageRepository
}

/**
 * [AppContainer] implementation that provides instance of [SessionRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {

    override val messageRepository: MessageRepository by lazy {
        MessageRepository(
            ChatXAppDatabase.getDatabase(context).profileDao(),
            ChatXAppDatabase.getDatabase(context).messageDao(),
            ChatXAppDatabase.getDatabase(context).sessionDao(),
            okHttpProvider
        )
    }

    override val offlineChatRepository: ChatRepository by lazy {
        OfflineChatRepository(
            ChatXAppDatabase.getDatabase(context).chatsDao()
        )
    }

    override val onlineChatRepository: ChatRepository by lazy {
        OnlineChatRepository(
            ChatXAppDatabase.getDatabase(context).sessionDao(),
            offlineChatRepository,
            messageRepository,
            ChatXAppDatabase.getDatabase(context).messageDao(),
            ChatXAppDatabase.getDatabase(context).chatsDao(),
            okHttpProvider
        )
    }

    override val chatService: ChatService by lazy {
        ChatService(
            onlineChatRepository,
            okHttpProvider
        )
    }

    override val okHttpProvider: OkHttpProvider by lazy {
        OkHttpProvider("http://192.168.0.110:8080")
    }

    override val offlineSessionRepository: SessionRepository by lazy {
        OfflineSessionRepository(ChatXAppDatabase.getDatabase(context).sessionDao())
    }

    override val onlineSessionRepository: SessionRepository by lazy {
        OnlineSessionRepository(
            okHttpProvider,
            ChatXAppDatabase.getDatabase(context).sessionDao(),
            ChatXAppDatabase.getDatabase(context).profileDao()
        )
    }

    override val userProfileService: UserProfileService by lazy {
        UserProfileService(onlineSessionRepository, offlineSessionRepository)
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
object ChatXAppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            LoginScreenViewModel(
                userProfileService = application().container.userProfileService
            )
        }
        initializer {
            ChatSelectorViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                userProfileService = application().container.userProfileService,
                chatService = application().container.chatService
            )
        }
        initializer {
            ChatScreenViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                messageRepository = application().container.messageRepository,
                offlineChatRepository = application().container.offlineChatRepository,
                onlineChatRepository = application().container.onlineChatRepository,
                offlineSessionRepository = application().container.offlineSessionRepository,
                chatService = application().container.chatService
            )
        }
    }
}

fun CreationExtras.application(): ChatXApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as ChatXApplication)