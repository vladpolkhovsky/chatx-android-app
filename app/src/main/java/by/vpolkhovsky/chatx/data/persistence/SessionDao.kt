package by.vpolkhovsky.chatx.data.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import by.vpolkhovsky.chatx.data.persistence.entities.ProfileEntity
import by.vpolkhovsky.chatx.data.persistence.entities.SavedSessions
import by.vpolkhovsky.chatx.data.persistence.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(sessionEntity: SessionEntity)

    @Delete
    fun delete(sessionEntity: SessionEntity)

    @Transaction
    @Query("SELECT p.* FROM sessions s INNER JOIN profiles p ON p.id = s.id WHERE s.id = :id")
    fun getProfileById(id: Int): ProfileEntity?

    @Transaction
    @Query("SELECT * FROM sessions s WHERE s.id = :id")
    fun getSessionById(id: Int): SessionEntity?

    @Transaction
    @Query("SELECT * FROM sessions")
    fun getAllSessions(): Flow<List<SavedSessions>>

    @Transaction
    @Query("SELECT * FROM sessions")
    fun getAllSessionsNow(): List<SavedSessions>
}