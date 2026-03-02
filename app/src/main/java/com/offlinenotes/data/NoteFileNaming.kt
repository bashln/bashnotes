package com.offlinenotes.data

import com.offlinenotes.domain.NoteKind
import java.io.IOException

object NoteFileNaming {
    fun ensureExtension(name: String, kind: NoteKind): String {
        val base = name.ifBlank { "nota" }
        return when (kind) {
            NoteKind.MARKDOWN_NOTE,
            NoteKind.MARKDOWN_TASKS -> if (base.endsWith(".md")) base else "$base.md"
            NoteKind.ORG_NOTE -> if (base.endsWith(".org")) base else "$base.org"
        }
    }

    fun normalizeRename(currentName: String, newName: String): String {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            throw IOException("Nome invalido")
        }
        if (trimmed.endsWith(".md") || trimmed.endsWith(".org")) {
            return trimmed
        }

        val currentExt = if (currentName.endsWith(".org")) ".org" else ".md"
        return "$trimmed$currentExt"
    }

    fun isNoteFile(fileName: String): Boolean {
        val lower = fileName.lowercase()
        return lower.endsWith(".md") || lower.endsWith(".org")
    }
}
