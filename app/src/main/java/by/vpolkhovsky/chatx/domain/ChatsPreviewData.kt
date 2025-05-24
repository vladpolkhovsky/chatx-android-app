package by.vpolkhovsky.chatx.domain

import java.time.LocalDateTime

data class ChatsPreviewData(
    val id: Int,
    val chatName: String,
    val newMessageCount: Int,
    val participantCount: Int,
    val lastMessage: LastMessagePreviewData?,
    val chatCreationDate: LocalDateTime,
)

fun Int.abbreviateIfGreaterThan(threshold: Int): String {
    return if (this > threshold) "${threshold}+" else toString()
}