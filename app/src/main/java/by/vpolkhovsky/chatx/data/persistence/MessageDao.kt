package by.vpolkhovsky.chatx.data.persistence

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import by.vpolkhovsky.chatx.data.persistence.entities.MessageEntity
import by.vpolkhovsky.chatx.data.persistence.entities.MessageFileEntity
import by.vpolkhovsky.chatx.data.persistence.entities.ProfileEntity

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveMessage(messageEntity: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveMessageFile(messageFileEntity: MessageFileEntity): Long

    @Query("select * from message where chat_id = :chatId order by created_at")
    fun loadAllChatMessages(chatId: Int): List<MessageWithProfileAndFiles>

    @Query("select * from message m where m.chat_id = :chatId and m.id in (:messageIds) order by created_at")
    fun loadChatMessagesByIds(chatId: Int, messageIds: List<Int>): List<MessageWithProfileAndFiles>

    @Query("select * from message m where m.id in (:messageIds) order by created_at")
    fun loadChatMessagesByIds(messageIds: List<Int>): List<MessageWithProfileAndFiles>

    @Query("select * from message m where m.chat_id = :chatId order by created_at desc limit 1")
    fun findLastMessageBy(chatId: Int): MessageWithProfileAndFiles?
}

data class MessageWithProfileAndFiles(
    @Embedded val messageEntity: MessageEntity,
    @Relation(
        parentColumn = "from_user_id",
        entityColumn = "id"
    )
    val fromUser: ProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "message_id"
    )
    val files: List<MessageFileEntity>
)