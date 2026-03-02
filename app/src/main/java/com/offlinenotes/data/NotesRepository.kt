package com.offlinenotes.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.offlinenotes.domain.NoteKind
import com.offlinenotes.domain.NoteMeta
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesRepository(private val context: Context) {
    private val quickNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")

    suspend fun listNotes(rootUri: Uri): List<NoteMeta> = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, rootUri) ?: return@withContext emptyList()
        val notes = mutableListOf<NoteMeta>()

        fun walk(directory: DocumentFile, prefix: String) {
            directory.listFiles().forEach { child ->
                if (child.isDirectory) {
                    val nextPrefix = if (prefix.isBlank()) {
                        child.name.orEmpty()
                    } else {
                        "$prefix/${child.name.orEmpty()}"
                    }
                    walk(child, nextPrefix)
                } else if (child.isFile) {
                    val fileName = child.name.orEmpty()
                    if (!NoteFileNaming.isNoteFile(fileName)) {
                        return@forEach
                    }

                    val relative = if (prefix.isBlank()) fileName else "$prefix/$fileName"
                    notes += NoteMeta(
                        name = fileName,
                        uri = child.uri,
                        relativePath = relative,
                        lastModified = child.lastModified().takeIf { it > 0L }
                    )
                }
            }
        }

        walk(root, "")

        notes.sortedWith(
            compareByDescending<NoteMeta> { it.lastModified ?: Long.MIN_VALUE }
                .thenBy { it.name.lowercase() }
        )
    }

    suspend fun createQuickNote(rootUri: Uri, kind: NoteKind): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val root = DocumentFile.fromTreeUri(context, rootUri)
                ?: throw IOException("Pasta raiz invalida")

            val base = LocalDateTime.now().format(quickNameFormatter)
            val extension = if (kind == NoteKind.ORG_NOTE) ".org" else ".md"
            var counter = 0
            var candidate = "$base$extension"

            while (root.findFile(candidate) != null) {
                counter += 1
                candidate = "$base-${counter.toString().padStart(2, '0')}$extension"
            }

            val mimeType = if (extension == ".org") "text/plain" else "text/markdown"
            val file = root.createFile(mimeType, candidate)
                ?: throw IOException("Nao foi possivel criar a nota")

            file.uri
        }
    }

    suspend fun readNote(noteUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(noteUri)?.bufferedReader()?.use { it.readText() }
                ?: ""
        }
    }

    suspend fun writeNote(noteUri: Uri, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(noteUri, "wt")?.bufferedWriter()?.use { writer ->
                writer.write(content)
            } ?: throw IOException("Falha ao abrir arquivo para escrita")
        }
    }

    suspend fun renameNote(noteUri: Uri, newName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = DocumentFile.fromSingleUri(context, noteUri)
                ?: throw IOException("Arquivo nao encontrado")

            val currentName = file.name.orEmpty()
            val targetName = NoteFileNaming.normalizeRename(currentName, newName)
            if (!NoteFileNaming.isNoteFile(targetName)) {
                throw IOException("Extensao invalida. Use .md ou .org")
            }

            if (!file.renameTo(targetName)) {
                throw IOException("Falha ao renomear arquivo")
            }
        }
    }

    suspend fun deleteNote(noteUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = DocumentFile.fromSingleUri(context, noteUri)
                ?: throw IOException("Arquivo nao encontrado")
            if (!file.delete()) {
                throw IOException("Falha ao deletar arquivo")
            }
        }
    }

}
