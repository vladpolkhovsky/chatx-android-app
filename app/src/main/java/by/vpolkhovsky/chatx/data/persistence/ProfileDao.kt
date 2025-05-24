package by.vpolkhovsky.chatx.data.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import by.vpolkhovsky.chatx.data.persistence.entities.ProfileEntity

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(profileEntity: ProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(profileEntities: Set<ProfileEntity>)

    @Delete
    fun delete(profileEntity: ProfileEntity)

    @Transaction
    @Query("SELECT * FROM profiles p WHERE p.id = :id")
    fun getProfileById(id: Int): ProfileEntity?
}