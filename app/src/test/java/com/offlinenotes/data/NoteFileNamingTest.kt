package com.offlinenotes.data

import com.offlinenotes.domain.NoteKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteFileNamingTest {

    @Test
    fun `ensureExtension adds markdown extension for default note`() {
        val value = NoteFileNaming.ensureExtension("daily", NoteKind.MARKDOWN_NOTE)
        assertEquals("daily.md", value)
    }

    @Test
    fun `ensureExtension keeps explicit org extension`() {
        val value = NoteFileNaming.ensureExtension("tasks.org", NoteKind.ORG_NOTE)
        assertEquals("tasks.org", value)
    }

    @Test
    fun `ensureExtension uses default name when blank`() {
        val value = NoteFileNaming.ensureExtension("", NoteKind.MARKDOWN_TASKS)
        assertEquals("nota.md", value)
    }

    @Test
    fun `normalizeRename appends current extension when omitted`() {
        val value = NoteFileNaming.normalizeRename("journal.org", "week-01")
        assertEquals("week-01.org", value)
    }

    @Test
    fun `normalizeRename preserves original extension`() {
        val value = NoteFileNaming.normalizeRename("journal.md", "archive.org")
        assertEquals("archive.md", value)
    }

    @Test
    fun `normalizeRename replaces invalid characters`() {
        val value = NoteFileNaming.normalizeRename("journal.org", "inv:alid/name")
        assertEquals("inv_alid_name.org", value)
    }

    @Test(expected = java.io.IOException::class)
    fun `normalizeRename fails for blank name`() {
        NoteFileNaming.normalizeRename("journal.md", "   ")
    }

    @Test
    fun `isNoteFile accepts md and org case-insensitive`() {
        assertTrue(NoteFileNaming.isNoteFile("A.MD"))
        assertTrue(NoteFileNaming.isNoteFile("b.Org"))
        assertFalse(NoteFileNaming.isNoteFile("image.png"))
    }
}
