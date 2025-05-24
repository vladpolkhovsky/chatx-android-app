package by.vpolkhovsky.chatx.data.persistence.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDateTime

@Entity(tableName = "chats")
data class ChatEntity (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo("chat_id") val chatId: Int,
    @ColumnInfo("profile_id") val profileId: Int,
    @ColumnInfo("chat_name") val chatName: String,
    @ColumnInfo("created_at") val createAt: Long,
    @ColumnInfo("part_count") val participants: Int
)

data class ProfileWithChats(
    @Embedded val profileEntity: ProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "profile_id"
    )
    val chatEntity: List<ChatEntity>
)