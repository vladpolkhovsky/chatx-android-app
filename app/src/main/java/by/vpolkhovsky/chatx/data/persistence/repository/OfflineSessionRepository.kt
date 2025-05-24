package by.vpolkhovsky.chatx.data.persistence.repository

import by.vpolkhovsky.chatx.data.persistence.SessionDao
import by.vpolkhovsky.chatx.domain.ProfileData

class OfflineSessionRepository(
    val sessionDao: SessionDao
) : SessionRepository {

    override fun getProfileData(id: Int): ProfileData? {
        return sessionDao.getProfileById(id)
            ?.let { entity -> ProfileData(entity.id, entity.username) }
    }

    override fun loadSessions(): List<ProfileData> {
        val sessions = sessionDao.getAllSessionsNow()
            .map { session -> session.profileEntity }
            .map { entity -> ProfileData(entity.id, entity.username) }
        return sessions
    }

    override fun deleteSession(id: Int) {
        sessionDao.getSessionById(id)?.let {
            sessionDao.delete(it)
        }
    }
}