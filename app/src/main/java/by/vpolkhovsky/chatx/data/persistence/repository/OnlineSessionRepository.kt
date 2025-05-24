package by.vpolkhovsky.chatx.data.persistence.repository

import by.vpolkhovsky.chatx.data.http.OkHttpProvider
import by.vpolkhovsky.chatx.data.persistence.ProfileDao
import by.vpolkhovsky.chatx.data.persistence.SessionDao
import by.vpolkhovsky.chatx.data.persistence.entities.ProfileEntity
import by.vpolkhovsky.chatx.data.persistence.entities.SessionEntity
import by.vpolkhovsky.chatx.domain.ProfileData

class OnlineSessionRepository(
    val httpClient: OkHttpProvider,
    val sessionDao: SessionDao,
    val profilesDao: ProfileDao
) : SessionRepository {

    override fun tryLogin(
        username: String,
        password: String
    ): ProfileData? {
        val dtoToToken = httpClient.getClient().login(username, password)

        if (dtoToToken == null) {
            return null
        }

        val userDto = dtoToToken.first
        val token = dtoToToken.second

        sessionDao.save(SessionEntity(userDto.id, token))
        profilesDao.save(ProfileEntity(userDto.id, userDto.username))

        return ProfileData(userDto.id, userDto.username)
    }

    override fun isTokenAlive(id: Int): Boolean {
        val sessionById = sessionDao.getSessionById(id)

        if (sessionById == null) {
            return false
        }

        return httpClient.getClient()
            .isTokenAlive(sessionById.jwtToken)
    }
}