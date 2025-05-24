package by.vpolkhovsky.chatx.data.persistence.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "jwt_token") val jwtToken: String
) {

}

data class SavedSessions(
    @Embedded val sessionEntity: SessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val profileEntity: ProfileEntity
)