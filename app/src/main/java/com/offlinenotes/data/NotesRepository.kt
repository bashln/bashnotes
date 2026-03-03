package com.offlinenotes.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.offlinenotes.domain.NoteKind
import com.offlinenotes.domain.NoteMeta
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesRepository(private val context: Context) {
    private val tag = "NotesRepo"
    private val quickNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")

    suspend fun listNotes(rootUri: Uri): List<NoteMeta> = withContext(Dispatchers.IO) {
        runSafResult("Falha ao listar notas") {
            val root = DocumentFile.fromTreeUri(context, rootUri)
                ?: throw IOException("Pasta raiz invalida")
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
        }.getOrElse { throw it }
    }

    suspend fun createQuickNote(rootUri: Uri, kind: NoteKind): Result<Uri> = withContext(Dispatchers.IO) {
        runSafResult("Nao foi possivel criar a nota") {
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

            val mimeType = if (extension == ".org") "application/octet-stream" else "text/markdown"
            val file = root.createFile(mimeType, candidate)
                ?: throw IOException("Sem permissao de escrita nesta pasta. Selecione outra pasta.")

            Log.d(tag, "createQuickNote: candidate=$candidate mimeType=$mimeType createdName=${file.name} uri=${file.uri}")
            var finalUri = file.uri

            if (extension == ".org") {
                val createdName = file.name.orEmpty()
                if (createdName != candidate) {
                    Log.d(tag, "createQuickNote: name mismatch '$createdName' -> '$candidate', renaming")
                    val renamedUri = DocumentsContract.renameDocument(
                        context.contentResolver, file.uri, candidate
                    )
                    if (renamedUri != null) {
                        Log.d(tag, "createQuickNote: rename ok newUri=$renamedUri")
                        finalUri = renamedUri
                    } else {
                        Log.w(tag, "createQuickNote: rename failed, keeping uri=$finalUri name=$createdName")
                    }
                }
            }

            Log.d(tag, "createQuickNote: returning finalUri=$finalUri")
            finalUri
        }
    }

    suspend fun checkWritableRoot(rootUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runSafResult("Falha ao validar pasta") {
            val root = DocumentFile.fromTreeUri(context, rootUri)
                ?: throw IOException("Pasta raiz invalida")

            val probeName = "offlinenotes_write_probe_${System.currentTimeMillis()}"
            val probe = root.createFile("text/plain", probeName)
                ?: throw IOException("Sem permissao de escrita nesta pasta. Selecione outra pasta.")

            runCatching { probe.delete() }
            Unit
        }
    }

    suspend fun readNote(noteUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        Log.d(tag, "readNote: uri=$noteUri")
        runSafResult("Falha ao abrir nota") {
            context.contentResolver.openInputStream(noteUri)?.bufferedReader()?.use { it.readText() }
                ?: ""
        }
    }

    suspend fun writeNote(noteUri: Uri, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(tag, "writeNote: uri=$noteUri length=${content.length}")
        runSafResult("Falha ao salvar nota") {
            context.contentResolver.openOutputStream(noteUri, "wt")?.bufferedWriter()?.use { writer ->
                writer.write(content)
            } ?: throw IOException("Falha ao abrir arquivo para escrita")
        }
    }

    suspend fun renameNote(noteUri: Uri, newName: String): Result<Uri> = withContext(Dispatchers.IO) {
        runSafResult("Falha ao renomear") {
            val currentName = resolveNoteName(noteUri)
            val targetName = NoteFileNaming.normalizeRename(currentName, newName)
            if (!NoteFileNaming.isNoteFile(targetName)) {
                throw IOException("Extensao invalida. Use .md ou .org")
            }

            DocumentsContract.renameDocument(context.contentResolver, noteUri, targetName)
                ?: throw IOException("Falha ao renomear arquivo")
        }
    }

    suspend fun deleteNote(noteUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runSafResult("Falha ao deletar") {
            val deleted = DocumentsContract.deleteDocument(context.contentResolver, noteUri)
            if (!deleted) {
                throw IOException("Falha ao deletar arquivo")
            }
        }
    }

    suspend fun getNoteName(noteUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runSafResult("Falha ao abrir nota") {
            resolveNoteName(noteUri)
        }
    }

    private fun resolveNoteName(noteUri: Uri): String {
        runCatching {
            context.contentResolver.query(
                noteUri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) {
                    val displayName = cursor.getString(index)
                    if (!displayName.isNullOrBlank()) {
                        return displayName
                    }
                }
            }
        }.onFailure { error ->
            Log.w(tag, "resolveNoteName query failed for uri=$noteUri", error)
        }

        val fromDocId = runCatching {
            DocumentsContract.getDocumentId(noteUri)
                .substringAfterLast('/')
                .substringAfterLast(':')
        }.getOrNull()

        if (!fromDocId.isNullOrBlank()) {
            return fromDocId
        }

        return noteUri.lastPathSegment?.substringAfterLast('/') ?: "Nota"
    }

    private fun <T> runSafResult(fallback: String, block: () -> T): Result<T> {
        return runCatching(block).recoverCatching { error ->
            Log.e(tag, "SAF error [${error.javaClass.simpleName}]: ${error.message}", error)
            val message = error.message.orEmpty().lowercase()
            if (message.contains("unsupported uri")) {
                throw IOException("Arquivo de nota invalido. Selecione a pasta novamente.")
            }
            when (error) {
                is SecurityException -> throw IOException(
                    "Sem permissao para acessar a pasta. Selecione a pasta novamente."
                )

                is IOException -> {
                    if (message.contains("eisdir") || message.contains("is a directory")) {
                        throw IOException("Arquivo de nota invalido. Selecione a pasta novamente.")
                    }
                    throw IOException(error.message ?: fallback)
                }

                else -> throw IOException(error.message ?: fallback)
            }
        }
    }

}
