package by.vpolkhovsky.chatx.data.persistence.repository

import by.vpolkhovsky.chatx.core.NotificationController
import by.vpolkhovsky.chatx.data.http.OkHttpProvider
import by.vpolkhovsky.chatx.data.http.localDateTimeFromMillis
import by.vpolkhovsky.chatx.data.persistence.ChatsDao
import by.vpolkhovsky.chatx.data.persistence.MessageDao
import by.vpolkhovsky.chatx.data.persistence.SessionDao
import by.vpolkhovsky.chatx.domain.ChatsPreviewData
import by.vpolkhovsky.chatx.domain.FileAttachmentsData
import by.vpolkhovsky.chatx.domain.LastMessagePreviewData
import by.vpolkhovsky.chatx.domain.ProfileData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import rest.ChatDto
import rest.ChatMemberDto
import rest.CreateChatRequest
import rest.UserDto

class OnlineChatRepository(
    val sessionDao: SessionDao,
    val offlineChatRepository: ChatRepository,
    val messageRepository: MessageRepository,
    val messageDao: MessageDao,
    val chatsDao: ChatsDao,
    val httpClient: OkHttpProvider,
) : ChatRepository {

    override fun fetchProfileChats(profileId: Int): List<ChatsPreviewData> {
        try {
            val chats = getClient(profileId).fetchUserChats()
            chats.map { toPreviewData(it) }
                .forEach { offlineChatRepository.updateChat(profileId, it) }
        } catch (e: Exception) {
            NotificationController.notify(
                "OnlineChatRepository fetchChats Exception",
                e.message.toString()
            )
        }
        return offlineChatRepository.fetchProfileChats(profileId)
    }

    override fun joinToChatByCode(profileId: Int, code: String) {
        getClient(profileId).joinToChatByCode(code)
    }

    override fun getChatCode(profileId: Int, chatId: Int): String? {
        return getClient(profileId).getChatCode(chatId)
    }

    override fun loadChatMembers(profileId: Int, chatId: Int): List<ProfileData> {
        return getClient(profileId).loadAllChatMembers(chatId).map {
            ProfileData(it.id, it.username)
        }
    }

    override fun saveChat(createChatRequest: CreateChatRequest) {
        getClient(createChatRequest.createdByUserId).createChat(createChatRequest);
    }

    override fun subscribeForJoinToChat(profileId: Int): Flow<ChatsPreviewData> {
        return getClient(profileId).collectSseNewChatInformation().map {
            toPreviewData(it)
        }
    }

    override fun subscribeForNewMessage(profileId: Int): Flow<ChatsPreviewData> {
        return getClient(profileId).collectSseNewMessages()
            .map {
                messageRepository.saveMessageToLocal(listOf(it))
                val chat = chatsDao.getByChatAndByProfileId(it.chatId, profileId)
                if (chat != null) {
                    val members = loadChatMembers(profileId, chat.chatId).map {
                        ChatMemberDto(
                            chat.id,
                            UserDto(it.id, it.username)
                        )
                    }
                    val chatDto = ChatDto(chat.chatId, chat.chatName, members, it.timestamp)
                    toPreviewData(chatDto)
                } else {
                    null
                }
            }.filterNotNull()
    }

    private fun getClient(profileId: Int): OkHttpProvider.Client {
        val sessionById = sessionDao.getSessionById(profileId)
        if (sessionById == null) {
            NotificationController.notify("NO SESSION", "Cant fin session for user $profileId");
            throw IllegalStateException("Cant fin session for user $profileId")
        }
        return httpClient.getClient(sessionById.jwtToken);
    }

    private fun toPreviewData(chatDto: ChatDto): ChatsPreviewData {
        val lastMessage = messageDao.findLastMessageBy(chatDto.id)?.let {
            LastMessagePreviewData(
                it.messageEntity.id,
                it.messageEntity.text,
                it.files.map { FileAttachmentsData(it.id, it.filename, it.fileSize) },
                ProfileData(it.fromUser.id, it.fromUser.username),
                localDateTimeFromMillis(it.messageEntity.createAt)
            )
        }
        return ChatsPreviewData(
            chatDto.id,
            chatDto.name,
            0,
            chatDto.members.size,
            lastMessage,
            localDateTimeFromMillis(chatDto.lastMessageTimestamp)
        )
    }
}