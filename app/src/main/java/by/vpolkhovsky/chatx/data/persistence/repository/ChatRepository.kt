package by.vpolkhovsky.chatx.data.persistence.repository

import by.vpolkhovsky.chatx.domain.ChatsPreviewData
import by.vpolkhovsky.chatx.domain.ProfileData
import kotlinx.coroutines.flow.Flow
import rest.CreateChatRequest

interface ChatRepository {

    fun loadChatInformation(profileId: Int, chatId: Int): ChatsPreviewData {
        TODO("NOT IMPLEMENTED")
    }

    fun loadChatMembers(profileId: Int, chatId: Int): List<ProfileData> {
        TODO("NOT IMPLEMENTED")
    }

    fun fetchProfileChats(profileId: Int): List<ChatsPreviewData> {
        TODO("NOT IMPLEMENTED")
    }

    fun updateChat(profileId: Int, chat: ChatsPreviewData) {
        TODO("NOT IMPLEMENTED")
    }

    fun saveChat(createChatRequest: CreateChatRequest) {
    }

    fun saveChats(createChatRequests: List<CreateChatRequest>) {
        createChatRequests.map(::saveChat)
    }

    fun subscribeForNewMessage(profileId: Int): Flow<ChatsPreviewData> {
        TODO("NOT IMPLEMENTED")
    }

    fun subscribeForJoinToChat(profileId: Int): Flow<ChatsPreviewData> {
        TODO("NOT IMPLEMENTED")
    }

    fun joinToChatByCode(profileId: Int, code: String) {
        TODO("NOT IMPLEMENTED")
    }

    fun getChatCode(profileId: Int, chatId: Int): String? {
        TODO("NOT IMPLEMENTED")
    }
}