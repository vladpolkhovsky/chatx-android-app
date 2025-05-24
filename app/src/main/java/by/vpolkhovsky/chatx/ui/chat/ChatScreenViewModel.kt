package by.vpolkhovsky.chatx.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import by.vpolkhovsky.chatx.data.ChatService
import by.vpolkhovsky.chatx.data.persistence.repository.ChatRepository
import by.vpolkhovsky.chatx.data.persistence.repository.MessageRepository
import by.vpolkhovsky.chatx.data.persistence.repository.SessionRepository
import by.vpolkhovsky.chatx.domain.FileAttachmentsData
import by.vpolkhovsky.chatx.domain.MessageData
import by.vpolkhovsky.chatx.domain.ProfileData
import by.vpolkhovsky.chatx.ui.navidation.ChatDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class ChatScreenViewModel(
    savedStateHandle: SavedStateHandle? = null,
    private val messageRepository: MessageRepository,
    private val offlineChatRepository: ChatRepository,
    private val onlineChatRepository: ChatRepository,
    private val offlineSessionRepository: SessionRepository,
    private val chatService: ChatService
) : ViewModel() {

    private val chatId: Int
    private val currentProfileId: Int

    private var _replyToMessage = mutableStateOf<Int?>(null)
    private var _needToScrollDown = mutableStateOf(false)

    val replyToMessage by _replyToMessage
    var needToScrollDown by _needToScrollDown

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiStateLoading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        val route = savedStateHandle
            ?.toRoute<ChatDestination>()

        currentProfileId = route!!.currentProfileId
        chatId = route!!.chatId

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val loadChatMessages = messageRepository.loadChatMessages(currentProfileId, chatId)

                val chatData = try {
                    offlineChatRepository.loadChatInformation(currentProfileId, chatId)
                } catch (_: Exception) {
                    onlineChatRepository.fetchProfileChats(currentProfileId)
                    offlineChatRepository.loadChatInformation(currentProfileId, chatId)
                }

                val chatMembers = onlineChatRepository.loadChatMembers(currentProfileId, chatId)
                val profileData = offlineSessionRepository.getProfileData(currentProfileId)!!
                val code = chatService.getChatCode(currentProfileId, chatId)

                _uiState.update {
                    ChatUiStateReady(
                        profileData,
                        chatData.chatName,
                        code,
                        chatMembers,
                        loadChatMessages
                    )

                }

                messageRepository.subscribeForNewMessages(currentProfileId)
                    .collect { newMessage ->
                        withContext(Dispatchers.Main) {
                            _uiState.update {
                                if (it is ChatUiStateReady) {
                                    needToScrollDown = true
                                    it.copy(
                                        messages = mutableListOf(*it.messages.toTypedArray())
                                            .apply { add(newMessage) }
                                    )
                                } else ChatUiStateLoading
                            }
                        }
                    }
            }
        }
    }

    fun sendMessage(text: String?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                messageRepository.sendMessage(
                    currentProfileId,
                    chatId,
                    text,
                    replyToMessage
                )
                clearReplyMessage()
            }
        }
    }

    fun selectReplyMessage(chatId: Int) {
        _replyToMessage.value = chatId
    }

    fun clearReplyMessage() {
        _replyToMessage.value = null
    }
}

sealed interface ChatUiState

object ChatUiStateLoading : ChatUiState

data class ChatUiStateReady(
    val currentProfile: ProfileData,
    val chatName: String,
    val code: String? = null,
    val chatMembers: List<ProfileData>,
    val messages: List<MessageData>
) : ChatUiState {
    companion object {
        val Default = defautlChatUiStateReady
    }
}

val defautlChatUiStateReady = ChatUiStateReady(
    currentProfile = ProfileData(
        id = 1,
        username = "Stapleton871"
    ),
    chatName = "Чат № 1",
    chatMembers = listOf(
        ProfileData(
            id = 1,
            username = "Stapleton871"
        ),
        ProfileData(
            id = 2,
            username = "Среднне имя профиля да-да"
        ),
        ProfileData(
            id = 3,
            username = "Очень длинное имя профиля вообще капец"
        )
    ),
    messages = listOf(
        MessageData(
            id = 150,
            from = ProfileData(
                id = 1,
                username = "Stapleton871"
            ),
            replyTo = null,
            text = "123-123",
            files = listOf(),
            date = LocalDateTime.now()
        ),
        MessageData(
            id = 100,
            from = ProfileData(
                id = 1,
                username = "Stapleton871"
            ),
            replyTo = null,
            text = "4442 223",
            files = listOf(),
            date = LocalDateTime.now()
        ),
        MessageData(
            id = 1,
            from = ProfileData(
                id = 1,
                username = "Stapleton871"
            ),
            replyTo = null,
            text = "Люба, Люба, Люба... Ну почему я, Купитман, должен объяснять тебе, Скрябиной, ментальность русского рабочего человека?",
            files = listOf(),
            date = LocalDateTime.now()
        ),
        MessageData(
            id = 2,
            from = ProfileData(
                id = 2,
                username = "Arpit Shukla"
            ),
            replyTo = null,
            text = "remember is used to preserve state across recompositions. If we are storing state inside ViewModel",
            files = listOf(
                FileAttachmentsData(
                    id = 1,
                    filename = "mutable value holder",
                    fileSizeBytes = 2048 * 6 * 50
                ),
                FileAttachmentsData(
                    id = 1,
                    filename = "mutable.mp4",
                    fileSizeBytes = 1024 * 8 * 10
                )
            ),
            date = LocalDateTime.now()
        ),
        MessageData(
            id = 3,
            from = ProfileData(
                id = 1,
                username = "Stapleton871"
            ),
            replyTo = MessageData(
                id = 1,
                from = ProfileData(
                    id = 1,
                    username = "Stapleton871"
                ),
                replyTo = null,
                text = "Люба, Люба, Люба... Ну почему я, Купитман, должен объяснять тебе, Скрябиной, ментальность русского рабочего человека?",
                files = listOf(),
                date = LocalDateTime.now().toLocalDate().atTime(17, 50)
            ),
            text = "It sets up an observer pattern (like a LiveData, StateFlow, etc.) where writes to the value inform the readers about the value change. So ViewModel has nothing to do with this observer pattern and that's why you still need to use mutable... functions in your ViewModel.",
            files = listOf(
                FileAttachmentsData(
                    id = 1,
                    filename = "mutableStateOf.jpg",
                    fileSizeBytes = 1024 * 8 * 10 * 9
                )
            ),
            date = LocalDateTime.now().minusDays(1).toLocalDate().atTime(12, 0)
        ),
        MessageData(
            id = 4,
            from = ProfileData(
                id = 3,
                username = "Akbolat Sadvakassov"
            ),
            replyTo = null,
            text = "It’s the question I asked Google, while was passing one of their trainings they’re advertising last month. It’s called Compose Camp. Where you can earn badges( the reason I attempted) and certificate.",
            files = listOf(),
            date = LocalDateTime.now().minusDays(3).toLocalDate().atStartOfDay()
        ),
    ),
)
