package by.vpolkhovsky.chatx.data.persistence.repository

import by.vpolkhovsky.chatx.domain.ProfileData
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    fun getProfileData(id: Int): ProfileData? {
        TODO("Not implemented")
    }

    fun tryLogin(username: String, password: String): ProfileData? {
        TODO("Not implemented")
    }

    fun loadSessions(): List<ProfileData> {
        TODO("Not implemented")
    }

    fun isTokenAlive(id: Int): Boolean {
        TODO("Not implemented")
    }

    fun deleteSession(id: Int): Unit {
        TODO("Not implemented")
    }
}