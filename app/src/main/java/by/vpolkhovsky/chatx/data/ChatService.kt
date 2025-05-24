package by.vpolkhovsky.chatx.data

import by.vpolkhovsky.chatx.data.http.OkHttpProvider
import by.vpolkhovsky.chatx.data.persistence.repository.ChatRepository
import by.vpolkhovsky.chatx.domain.ChatsPreviewData
import kotlinx.coroutines.flow.Flow
import rest.CreateChatRequest

class ChatService(
    val onlineChatRepository: ChatRepository,
    val httpProvider: OkHttpProvider
) {

    fun getChatCode(profileId: Int, chatId: Int): String? {
        return onlineChatRepository.getChatCode(profileId, chatId)
    }

    fun createChatAttachedToProfile(profileId: Int, chatName: String) {
        onlineChatRepository.saveChat(CreateChatRequest(profileId, chatName))
    }

    fun joinToChatByCode(profileId: Int, code: String) {
        onlineChatRepository.joinToChatByCode(profileId, code)
    }

    fun fetchProfileChats(profileId: Int): List<ChatsPreviewData> {
        return onlineChatRepository.fetchProfileChats(profileId)
    }

    fun subscribeForJoinToChat(profileId: Int): Flow<ChatsPreviewData> {
        return onlineChatRepository.subscribeForJoinToChat(profileId)
    }

    fun subscribeForNewMessage(profileId: Int): Flow<ChatsPreviewData> {
        return onlineChatRepository.subscribeForNewMessage(profileId)
    }
}