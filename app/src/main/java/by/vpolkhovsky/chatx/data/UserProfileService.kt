package by.vpolkhovsky.chatx.data

import by.vpolkhovsky.chatx.core.NotificationController
import by.vpolkhovsky.chatx.data.persistence.repository.SessionRepository
import by.vpolkhovsky.chatx.domain.ProfileData

class UserProfileService(
    val onlineSessionRepository: SessionRepository,
    val offlineSessionRepository: SessionRepository
) {
    fun fetchActiveSessions(): List<ProfileData> {
        val activeProfiles = mutableListOf<ProfileData>()

        offlineSessionRepository.loadSessions().forEach { session ->
            try {
                if (onlineSessionRepository.isTokenAlive(session.id)) {
                    activeProfiles.add(session)
                } else {
                    offlineSessionRepository.deleteSession(session.id)
                }
            } catch (e: Exception) {
                NotificationController.notify("UserProfileService fetchActiveSessions", e.message.toString())
            }
        }

        return activeProfiles
    }

    fun tryLogin(username: String, password: String) =
        onlineSessionRepository.tryLogin(username, password)

    fun logout(profileId: Int) = offlineSessionRepository.deleteSession(profileId)
}