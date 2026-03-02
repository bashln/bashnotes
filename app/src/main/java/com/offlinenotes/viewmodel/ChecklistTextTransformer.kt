package com.offlinenotes.viewmodel

object ChecklistTextTransformer {
    fun extractChecklistLines(text: String): List<ChecklistLine> {
        return text.lines().mapIndexedNotNull { index, line ->
            when {
                line.startsWith("- [ ]") -> {
                    ChecklistLine(index = index, checked = false, text = line.removePrefix("- [ ]").trim())
                }

                line.startsWith("- [x]") -> {
                    ChecklistLine(index = index, checked = true, text = line.removePrefix("- [x]").trim())
                }

                line.startsWith("- [X]") -> {
                    ChecklistLine(index = index, checked = true, text = line.removePrefix("- [X]").trim())
                }

                else -> null
            }
        }
    }

    fun toggleLine(text: String, lineIndex: Int): String {
        val lines = text.split("\n").toMutableList()
        if (lineIndex !in lines.indices) {
            return text
        }

        val current = lines[lineIndex]
        val toggled = when {
            current.startsWith("- [ ]") -> current.replaceFirst("- [ ]", "- [x]")
            current.startsWith("- [x]") -> current.replaceFirst("- [x]", "- [ ]")
            current.startsWith("- [X]") -> current.replaceFirst("- [X]", "- [ ]")
            else -> current
        }
        lines[lineIndex] = toggled
        return lines.joinToString("\n")
    }
}
