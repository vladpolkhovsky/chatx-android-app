package by.vpolkhovsky.chatx.data.persistence.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo("username") val username: String
)