package by.vpolkhovsky.chatx.data.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import by.vpolkhovsky.chatx.data.persistence.entities.ChatEntity
import by.vpolkhovsky.chatx.data.persistence.entities.MessageEntity
import by.vpolkhovsky.chatx.data.persistence.entities.MessageFileEntity
import by.vpolkhovsky.chatx.data.persistence.entities.ProfileEntity
import by.vpolkhovsky.chatx.data.persistence.entities.SessionEntity

@Database(
    entities = [
        ProfileEntity::class,
        SessionEntity::class,
        MessageEntity::class,
        MessageFileEntity::class,
        ChatEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ChatXAppDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao

    abstract fun profileDao(): ProfileDao

    abstract fun chatsDao(): ChatsDao

    abstract fun messageDao(): MessageDao

    companion object {

        @Volatile
        private var Instance: ChatXAppDatabase? = null

        fun getDatabase(context: Context): ChatXAppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ChatXAppDatabase::class.java, "chatx_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}