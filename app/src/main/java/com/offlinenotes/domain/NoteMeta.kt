package com.offlinenotes.domain

import android.net.Uri

data class NoteMeta(
    val name: String,
    val uri: Uri,
    val relativePath: String,
    val lastModified: Long?
)

enum class NoteKind {
    MARKDOWN_NOTE,
    MARKDOWN_TASKS,
    ORG_NOTE
}
