package by.vpolkhovsky.chatx.domain

data class FileAttachmentsData(
    val id: Int,
    val filename: String,
    val fileSizeBytes: Long
) {
}