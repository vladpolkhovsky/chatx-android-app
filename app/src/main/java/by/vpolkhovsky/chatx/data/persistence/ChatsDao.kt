package by.vpolkhovsky.chatx.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import by.vpolkhovsky.chatx.data.persistence.entities.ChatEntity
import by.vpolkhovsky.chatx.data.persistence.entities.ProfileWithChats

@Dao
interface ChatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(chatEntity: ChatEntity): Long

    @Query("SELECT * from chats c where c.chat_id = :chatId and c.profile_id = :profileId")
    fun getByChatAndByProfileId(chatId: Int, profileId: Int): ChatEntity?

    @Transaction
    @Query("SELECT * from profiles p where p.id = :profileId")
    fun getProfileChats(profileId: Int): ProfileWithChats
}
