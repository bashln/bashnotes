package com.offlinenotes.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistTextTransformerTest {

    @Test
    fun `extractChecklistLines returns checklist entries only`() {
        val text = """
            Header
            - [ ] Item 1
            body
            - [x] Item 2
            - [X] Item 3
        """.trimIndent()

        val lines = ChecklistTextTransformer.extractChecklistLines(text)

        assertEquals(3, lines.size)
        assertEquals(1, lines[0].index)
        assertEquals("Item 1", lines[0].text)
        assertTrue(!lines[0].checked)
        assertEquals(3, lines[1].index)
        assertTrue(lines[1].checked)
        assertEquals(4, lines[2].index)
        assertTrue(lines[2].checked)
    }

    @Test
    fun `toggleLine flips unchecked to checked`() {
        val text = "- [ ] Item"

        val toggled = ChecklistTextTransformer.toggleLine(text, 0)

        assertEquals("- [x] Item", toggled)
    }

    @Test
    fun `toggleLine flips checked to unchecked`() {
        val text = "- [x] Item"

        val toggled = ChecklistTextTransformer.toggleLine(text, 0)

        assertEquals("- [ ] Item", toggled)
    }

    @Test
    fun `toggleLine ignores out of range indexes`() {
        val text = "- [ ] One\n- [ ] Two"

        val toggled = ChecklistTextTransformer.toggleLine(text, 5)

        assertEquals(text, toggled)
    }
}
