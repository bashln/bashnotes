package com.offlinenotes.data

import java.io.IOException

object NoteFileNaming {
    private val invalidChars = Regex("[\\\\/:*?\"<>|]")

    fun normalizeRename(currentName: String, newName: String): String {
        val currentExt = if (currentName.endsWith(".org")) ".org" else ".md"
        val withoutExt = newName.trim()
            .removeSuffix(".md")
            .removeSuffix(".org")
        val sanitized = sanitizeBaseName(withoutExt)
        if (sanitized.isBlank()) {
            throw IOException("Nome invalido")
        }
        return "$sanitized$currentExt"
    }

    fun isNoteFile(fileName: String): Boolean {
        val lower = fileName.lowercase()
        return lower.endsWith(".md") || lower.endsWith(".org")
    }

    fun sanitizeBaseName(value: String): String {
        return invalidChars.replace(value.trim(), "_")
    }
}
