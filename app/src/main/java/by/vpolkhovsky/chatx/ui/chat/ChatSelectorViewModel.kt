package by.vpolkhovsky.chatx.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import by.vpolkhovsky.chatx.data.ChatService
import by.vpolkhovsky.chatx.data.UserProfileService
import by.vpolkhovsky.chatx.domain.ChatsPreviewData
import by.vpolkhovsky.chatx.domain.FileAttachmentsData
import by.vpolkhovsky.chatx.domain.LastMessagePreviewData
import by.vpolkhovsky.chatx.domain.ProfileData
import by.vpolkhovsky.chatx.ui.navidation.ChatSelectionDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class ChatSelectorViewModel(
    savedStateHandle: SavedStateHandle? = null,
    private val userProfileService: UserProfileService,
    private val chatService: ChatService
) : ViewModel() {

    private val _uiState: MutableStateFlow<ChatSelectorUiState> =
        MutableStateFlow<ChatSelectorUiState>(LoadingChatSelectorUiState)

    val uiState: StateFlow<ChatSelectorUiState> = _uiState.asStateFlow()

    val selectedProfileId: Int

    init {
        val currentProfileId = savedStateHandle
            ?.toRoute<ChatSelectionDestination>()
            ?.currentProfileId

        selectedProfileId = currentProfileId!!

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                launch {
                    val sessions = userProfileService.fetchActiveSessions()
                    val fetchProfileChats = chatService.fetchProfileChats(selectedProfileId)

                    val newChatFlow = chatService.subscribeForJoinToChat(selectedProfileId)
                    val newMessageFlow = chatService.subscribeForNewMessage(selectedProfileId)

                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            ReadyChatSelectorUiState(
                                currentProfileId = selectedProfileId,
                                loadedProfiles = sessions,
                                chats = fetchProfileChats
                            )
                        }
                    }

                    launch {
                        newChatFlow.collect { chatPreviewData ->
                            onJoinToNewChat(chatPreviewData)
                        }
                    }

                    launch {
                        newMessageFlow.collect { updatedChatPreview ->
                            onNewMessage(updatedChatPreview)
                        }
                    }
                }
            }
        }
    }

    fun onJoinToNewChat(chatsPreviewData: ChatsPreviewData) {
        _uiState.update {
            if (it is ReadyChatSelectorUiState) {
                val newChatList = mutableListOf(*it.chats.toTypedArray()).apply {
                    add(0, chatsPreviewData)
                }
                it.copy(
                    currentProfileId = selectedProfileId,
                    loadedProfiles = it.loadedProfiles,
                    chats = newChatList
                )
            } else {
                it
            }
        }
    }

    fun onNewMessage(newMessageChatsPreviewData: ChatsPreviewData) {
        _uiState.update {
            if (it is ReadyChatSelectorUiState) {
                val chats =
                    mutableListOf(*it.chats.filterNot { it.id == newMessageChatsPreviewData.id }
                        .toTypedArray())
                        .apply {
                            add(0, newMessageChatsPreviewData)
                        }
                it.copy(
                    currentProfileId = selectedProfileId,
                    loadedProfiles = it.loadedProfiles,
                    chats = chats
                )
            } else {
                it
            }
        }
    }

    fun logout(onLogoutProcessed: () -> Unit) {
        _uiState.update {
            LoadingChatSelectorUiState
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userProfileService.logout(selectedProfileId)
                withContext(Dispatchers.Main) {
                    onLogoutProcessed()
                }
            }
        }
    }

    fun createNewChat(chatName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatService.createChatAttachedToProfile(selectedProfileId, chatName)
            }
        }
    }

    fun joinByCode(code: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatService.joinToChatByCode(selectedProfileId, code)
            }
        }
    }
}

sealed interface ChatSelectorUiState

object LoadingChatSelectorUiState : ChatSelectorUiState

data class ReadyChatSelectorUiState(
    val currentProfileId: Int,
    val loadedProfiles: List<ProfileData>,
    val chats: List<ChatsPreviewData>
) : ChatSelectorUiState {
    companion object {
        val Default: ReadyChatSelectorUiState
            get() = defaultState
    }
}

fun ChatSelectorUiState.getCurrentProfile(): ProfileData {
    this as ReadyChatSelectorUiState
    val foundProfile = this.loadedProfiles.find { it.id == this.currentProfileId }
    return foundProfile ?: this.loadedProfiles.first()
}

private val loadedProfilesData = listOf<ProfileData>(
    ProfileData(1, "Мандарин"),
    ProfileData(2, "Апельсин"),
    ProfileData(3, "ОченьДлинноеИмяПрофиляПрямОчень"),
    ProfileData(4, "Stapleton871")
)


val fromDefault = LastMessagePreviewData(
    id = 1,
    text = "Привет из пинска родные! Вот ма пару фото:",
    files = listOf(
        FileAttachmentsData(
            id = 1,
            filename = "Пинск_на_закате.jpeg",
            fileSizeBytes = 586000
        ),
        FileAttachmentsData(
            id = 2,
            filename = "На закате.jpeg",
            fileSizeBytes = 8789989
        ),
        FileAttachmentsData(
            id = 2,
            filename = "1231233.jpeg",
            fileSizeBytes = 8789989
        ),
        FileAttachmentsData(
            id = 2,
            filename = "Fake lol.jpeg",
            fileSizeBytes = 8789989
        )
    ),
    profile = ProfileData(
        id = 170,
        username = "Valdislav_Polkhovsky"
    ),
    date = LocalDateTime.now().minusDays(3)
)

val defaultState = ReadyChatSelectorUiState(
    currentProfileId = loadedProfilesData[1].id,
    loadedProfiles = loadedProfilesData,
    chats = listOf(
        ChatsPreviewData(
            id = 1,
            chatName = "Chat #1 for help",
            newMessageCount = 30,
            participantCount = 2,
            lastMessage = null,
            chatCreationDate = LocalDateTime.now()
        ),
        ChatsPreviewData(
            id = 2,
            chatName = "Chat #2 test group",
            newMessageCount = 10,
            participantCount = 2,
            lastMessage = LastMessagePreviewData(
                id = 1,
                text = "Привет из пинска родные! Вот ма пару фото:",
                files = listOf(),
                profile = ProfileData(
                    id = 4,
                    username = "Даша Кухновец"
                ),
                date = LocalDateTime.now().minusDays(45)
            ),
            chatCreationDate = LocalDateTime.now().minusDays(10)
        ),
        ChatsPreviewData(
            id = 2,
            chatName = "Chat #2 test group",
            newMessageCount = 190,
            participantCount = 2,
            lastMessage = LastMessagePreviewData(
                id = 70,
                text = "Люба, Люба, Люба... Ну почему я, Купитман, должен объяснять тебе, Скрябиной, ментальность русского рабочего человека?",
                files = listOf(),
                profile = ProfileData(
                    id = 4,
                    username = "Иван Натанович Купитман"
                ),
                date = LocalDateTime.now().minusDays(45)
            ),
            chatCreationDate = LocalDateTime.now().minusDays(10)
        ),
        ChatsPreviewData(
            id = 3,
            chatName = "ChatX group",
            newMessageCount = 10,
            participantCount = 30,
            lastMessage = fromDefault,
            chatCreationDate = LocalDateTime.now().minusMonths(4)
        ),
        ChatsPreviewData(
            id = 4,
            chatName = "Very long chat name for test ui view",
            newMessageCount = 1000,
            participantCount = 22,
            lastMessage = fromDefault,
            chatCreationDate = LocalDateTime.now().minusYears(3).minusDays(15)
        ),
        ChatsPreviewData(
            id = 4,
            chatName = "OTHER VERY LONG FOR PREVIEW Very long chat name for test ui view",
            newMessageCount = 19,
            participantCount = 2200,
            lastMessage = fromDefault,
            chatCreationDate = LocalDateTime.now().minusYears(3).minusDays(15)
        ),

        ChatsPreviewData(
            id = 3,
            chatName = "ChatX group",
            newMessageCount = 10,
            participantCount = 30,
            lastMessage = fromDefault,
            chatCreationDate = LocalDateTime.now().minusMonths(4)
        ),
        ChatsPreviewData(
            id = 4,
            chatName = "Very long chat name for test ui view",
            newMessageCount = 1000,
            participantCount = 22,
            lastMessage = fromDefault,
            chatCreationDate = LocalDateTime.now().minusYears(3).minusDays(15)
        ),
        ChatsPreviewData(
            id = 4,
            chatName = "OTHER VERY LONG FOR PREVIEW Very long chat name for test ui view",
            newMessageCount = 19,
            participantCount = 2200,
            lastMessage = fromDefault,
            chatCreationDate = LocalDateTime.now().minusYears(3).minusDays(15)
        ),

        ChatsPreviewData(
            id = 3,
            chatName = "ChatX group",
            newMessageCount = 10,
            participantCount = 30,
            lastMessage = fromDefault,
            chatCreationDate = LocalDateTime.now().minusMonths(4)
        ),
        ChatsPreviewData(
            id = 4,
            chatName = "Very long chat name for test ui view",
            newMessageCount = 1000,
            participantCount = 22,
            lastMessage = fromDefault,
            chatCreationDate = LocalDateTime.now().minusYears(3).minusDays(15)
        ),
        ChatsPreviewData(
            id = 4,
            chatName = "OTHER VERY LONG FOR PREVIEW Very long chat name for test ui view",
            newMessageCount = 19,
            participantCount = 2200,
            lastMessage = fromDefault,
            chatCreationDate = LocalDateTime.now().minusYears(3).minusDays(15)
        )
    )
)

private val timeFormatterThisDay: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val timeFormatterThisYear: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM HH:mm")
private val timeFormatterOther: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

fun LocalDateTime.toDateString(): String {
    val atStartOfDay = LocalDateTime.now().toLocalDate().atStartOfDay()
    if (atStartOfDay.isBefore(this)) {
        return this.format(timeFormatterThisDay)
    }
    val atStartOfYear = LocalDateTime.of(atStartOfDay.year, 1, 1, 0, 0)
    if (atStartOfYear.isBefore(this)) {
        return this.format(timeFormatterThisYear)
    }
    return this.format(timeFormatterOther)
}