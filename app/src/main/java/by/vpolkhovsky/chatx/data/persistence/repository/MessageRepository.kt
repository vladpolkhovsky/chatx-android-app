package by.vpolkhovsky.chatx.data.persistence.repository

import by.vpolkhovsky.chatx.core.NotificationController
import by.vpolkhovsky.chatx.data.http.OkHttpProvider
import by.vpolkhovsky.chatx.data.http.localDateTimeFromMillis
import by.vpolkhovsky.chatx.data.persistence.MessageDao
import by.vpolkhovsky.chatx.data.persistence.MessageWithProfileAndFiles
import by.vpolkhovsky.chatx.data.persistence.ProfileDao
import by.vpolkhovsky.chatx.data.persistence.SessionDao
import by.vpolkhovsky.chatx.data.persistence.entities.MessageEntity
import by.vpolkhovsky.chatx.data.persistence.entities.MessageFileEntity
import by.vpolkhovsky.chatx.data.persistence.entities.ProfileEntity
import by.vpolkhovsky.chatx.domain.FileAttachmentsData
import by.vpolkhovsky.chatx.domain.MessageData
import by.vpolkhovsky.chatx.domain.ProfileData
import event.MessageDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import rest.NewMessageRequest

class MessageRepository(
    private val profilesDao: ProfileDao,
    private val messageDao: MessageDao,
    private val sessionDao: SessionDao,
    private val httpClient: OkHttpProvider
) {

    fun sendMessage(fromUserId: Int, chatId: Int, text: String?, replyTo: Int?) {
        val request = NewMessageRequest(
            fromUserId,
            chatId,
            replyTo,
            text,
            listOf()
        )
        getClient(profileId = fromUserId).sendMessage(request)
    }

    fun loadChatMessages(profileId: Int, chatId: Int): List<MessageData> {
        val loadAllMessages = getClient(profileId).loadAllMessages(chatId)
        saveMessageToLocal(loadAllMessages)
        return fetchMessages(chatId, null)
    }

    private fun fetchMessages(
        chatId: Int,
        messageIds: List<Int>?
    ): List<MessageData> {
        var messages = if (messageIds != null) messageDao.loadChatMessagesByIds(chatId, messageIds)
        else messageDao.loadAllChatMessages(chatId)

        var idToInformation = messages.associate {
            it.messageEntity.id to it
        }

        val notPresentedIds = messages
            .filter { it.messageEntity.replyTo != null }
            .filterNot { it.messageEntity.replyTo in idToInformation.keys }
            .map { it.messageEntity.replyTo!! }

        if (notPresentedIds.isNotEmpty()) {
            val newMessagesIds = mutableListOf<Int>(*idToInformation.keys.toTypedArray()).apply {
                addAll(notPresentedIds)
                messageIds?.let { addAll(it) }
            }
            return fetchMessages(chatId, newMessagesIds)
        }

        return messages.map {
            makeMessageData(it.messageEntity.id, idToInformation)
        }
    }

    public fun saveMessageToLocal(rawMessages: List<MessageDto>) {
        val files = rawMessages
            .map { Pair(it.id, it.files) }
            .flatMap { (messageId, files) ->
                files.map {
                    MessageFileEntity(
                        it.id,
                        messageId,
                        it.name,
                        it.size
                    )
                }
            }

        files.forEach { messageDao.saveMessageFile(it) }

        val messages = rawMessages
            .flatMap {
                val flatMapReply: MutableList<MessageDto> = mutableListOf(it)
                val profiles: MutableSet<ProfileEntity> =
                    mutableSetOf(ProfileEntity(it.from.id, it.from.username))

                var reply = it.replyTo
                while (reply != null) {
                    flatMapReply.add(reply)
                    profiles.add(ProfileEntity(reply.from.id, reply.from.username))
                    reply = reply.replyTo
                }

                profilesDao.saveAll(profiles)

                flatMapReply
            }
            .map {
                MessageEntity(
                    it.id,
                    it.chatId,
                    it.text,
                    it.replyTo?.id,
                    it.from.id,
                    it.timestamp
                )
            }

        messages.forEach { messageDao.saveMessage(it) }
    }

    fun subscribeForNewMessages(profileId: Int): Flow<MessageData> {
        return getClient(profileId).collectSseNewMessages()
            .map {
                saveMessageToLocal(listOf(it))
                fetchMessages(it.chatId, listOf(it.id)).firstOrNull { mapped -> mapped.id == it.id }
            }
            .filterNotNull()
    }

    private fun getClient(profileId: Int): OkHttpProvider.Client {
        val sessionById = sessionDao.getSessionById(profileId)
        if (sessionById == null) {
            NotificationController.notify("NO SESSION", "Cant fin session for user $profileId");
            throw IllegalStateException("Cant fin session for user $profileId")
        }
        return httpClient.getClient(sessionById.jwtToken);
    }

    private fun makeMessageData(
        id: Int,
        idToInformation: Map<Int, MessageWithProfileAndFiles>
    ): MessageData {

        val profileData = idToInformation.get(id)!!.fromUser.let {
            ProfileData(
                it.id,
                it.username
            )
        }

        val reply = idToInformation.get(id)!!.messageEntity.replyTo?.let {
            makeMessageData(it, idToInformation)
        }

        val text = idToInformation.get(id)!!.messageEntity.text
        val time = localDateTimeFromMillis(idToInformation.get(id)!!.messageEntity.createAt)
        val file = idToInformation.get(id)!!.files.map {
            FileAttachmentsData(it.id, it.filename, it.fileSize)
        }

        return MessageData(
            id,
            profileData,
            reply,
            text,
            file,
            time
        )
    }
}