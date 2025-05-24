package by.vpolkhovsky.chatx.domain

import java.time.LocalDateTime

data class LastMessagePreviewData(
    val id: Int,
    val text: String?,
    val files: List<FileAttachmentsData>,
    val profile: ProfileData,
    val date: LocalDateTime
) {
}