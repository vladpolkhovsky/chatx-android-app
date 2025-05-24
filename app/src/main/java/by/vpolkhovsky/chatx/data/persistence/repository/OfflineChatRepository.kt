package by.vpolkhovsky.chatx.data.persistence.repository

import by.vpolkhovsky.chatx.data.http.localDateTimeFromMillis
import by.vpolkhovsky.chatx.data.persistence.ChatsDao
import by.vpolkhovsky.chatx.data.persistence.entities.ChatEntity
import by.vpolkhovsky.chatx.domain.ChatsPreviewData
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class OfflineChatRepository(
    val chatsDao: ChatsDao
) : ChatRepository {

    override fun loadChatInformation(profileId: Int, chatId: Int): ChatsPreviewData {
        return chatsDao.getByChatAndByProfileId(chatId, profileId)?.let(this::mapToPreviewData)!!
    }

    override fun fetchProfileChats(profileId: Int): List<ChatsPreviewData> {
        return chatsDao.getProfileChats(profileId).chatEntity.map(this::mapToPreviewData)

    }

    override fun updateChat(
        profileId: Int,
        chat: ChatsPreviewData
    ) {
        val id = chatsDao.getByChatAndByProfileId(chat.id, profileId)?.id ?: 0
        chatsDao.save(
            ChatEntity(
                id,
                chat.id,
                profileId,
                chat.chatName,
                chat.chatCreationDate.toMillis(),
                chat.participantCount
            )
        )
    }

    private fun mapToPreviewData(it: ChatEntity): ChatsPreviewData {
        return ChatsPreviewData(
            it.chatId, it.chatName, 0, it.participants, null, localDateTimeFromMillis(it.createAt)
        )
    }
}


private fun LocalDateTime.toMillis(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
