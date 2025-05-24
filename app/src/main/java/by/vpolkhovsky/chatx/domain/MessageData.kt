package by.vpolkhovsky.chatx.domain

import java.time.LocalDateTime

data class MessageData(
    val id: Int,
    val from: ProfileData,
    val replyTo: MessageData?,
    val text: String?,
    val files: List<FileAttachmentsData>,
    val date: LocalDateTime
) {
}