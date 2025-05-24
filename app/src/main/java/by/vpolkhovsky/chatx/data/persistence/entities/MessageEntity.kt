package by.vpolkhovsky.chatx.data.persistence.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class MessageEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "chat_id") val chatId: Int,
    @ColumnInfo(name = "text") val text: String?,
    @ColumnInfo(name = "reply_to") val replyTo: Int?,
    @ColumnInfo(name = "from_user_id") val fromUserId: Int,
    @ColumnInfo(name = "created_at") val createAt: Long,
)

@Entity(tableName = "message_file")
data class MessageFileEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "message_id") val messageId: Int,
    @ColumnInfo(name = "filename") val filename: String,
    @ColumnInfo(name = "file_size") val fileSize: Long
)
