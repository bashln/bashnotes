package com.offlinenotes.ui.editor

internal sealed interface PreviewBlock {
    data class Heading(val level: Int, val text: String) : PreviewBlock
    data class Checklist(
        val checked: Boolean,
        val text: String,
        val indentLevel: Int
    ) : PreviewBlock

    data class Bullet(
        val text: String,
        val indentLevel: Int
    ) : PreviewBlock

    data class CodeBlock(
        val languageHint: String?,
        val content: String
    ) : PreviewBlock

    data class Paragraph(val text: String) : PreviewBlock
    data object Empty : PreviewBlock
}

internal fun parsePreviewBlocks(text: String, isOrg: Boolean): List<PreviewBlock> {
    if (text.isBlank()) {
        return listOf(PreviewBlock.Paragraph("Nota vazia"))
    }

    val headingPattern = if (isOrg) {
        Regex("^(\\*{1,6})\\s+(.+)$")
    } else {
        Regex("^(#{1,6})\\s+(.+)$")
    }
    val checklistPattern = Regex("^(\\s*)[-+*]\\s+\\[([ xX])]\\s+(.+)$")
    val bulletPattern = Regex("^(\\s*)[-+*]\\s+(.+)$")
    val orgCodeStart = Regex("^\\s*#\\+begin_src(?:\\s+(\\S+))?.*$", RegexOption.IGNORE_CASE)
    val orgCodeEnd = Regex("^\\s*#\\+end_src\\s*$", RegexOption.IGNORE_CASE)

    val lines = text.lines()
    val blocks = mutableListOf<PreviewBlock>()
    var index = 0

    while (index < lines.size) {
        val rawLine = lines[index]
        val line = rawLine.trimEnd()

        val markdownCodeStart = line.trimStart().takeIf { it.startsWith("```") }
        if (!isOrg && markdownCodeStart != null) {
            val language = markdownCodeStart.removePrefix("```").trim().ifBlank { null }
            val contentLines = mutableListOf<String>()
            index += 1
            while (index < lines.size && !lines[index].trimStart().startsWith("```")) {
                contentLines += lines[index]
                index += 1
            }
            if (index < lines.size) {
                index += 1
            }
            blocks += PreviewBlock.CodeBlock(
                languageHint = language,
                content = contentLines.joinToString("\n")
            )
            continue
        }

        val orgCodeMatch = if (isOrg) orgCodeStart.find(line) else null
        if (isOrg && orgCodeMatch != null) {
            val language = orgCodeMatch.groupValues.getOrNull(1)?.ifBlank { null }
            val contentLines = mutableListOf<String>()
            index += 1
            while (index < lines.size && !orgCodeEnd.matches(lines[index])) {
                contentLines += lines[index]
                index += 1
            }
            if (index < lines.size) {
                index += 1
            }
            blocks += PreviewBlock.CodeBlock(
                languageHint = language,
                content = contentLines.joinToString("\n")
            )
            continue
        }

        when {
            line.isBlank() -> blocks += PreviewBlock.Empty
            headingPattern.matches(line) -> {
                val match = headingPattern.find(line)!!
                val level = match.groupValues[1].length
                val content = match.groupValues[2].trim()
                blocks += PreviewBlock.Heading(level = level, text = content)
            }

            checklistPattern.matches(line) -> {
                val match = checklistPattern.find(line)!!
                val indent = toIndentLevel(match.groupValues[1])
                val checked = match.groupValues[2].equals("x", ignoreCase = true)
                val content = match.groupValues[3].trim()
                blocks += PreviewBlock.Checklist(
                    checked = checked,
                    text = content,
                    indentLevel = indent
                )
            }

            bulletPattern.matches(line) -> {
                val match = bulletPattern.find(line)!!
                val indent = toIndentLevel(match.groupValues[1])
                blocks += PreviewBlock.Bullet(
                    text = match.groupValues[2].trim(),
                    indentLevel = indent
                )
            }

            else -> blocks += PreviewBlock.Paragraph(text = line)
        }

        index += 1
    }

    return blocks
}

private fun toIndentLevel(leadingWhitespace: String): Int {
    val count = leadingWhitespace.fold(0) { acc, char ->
        acc + if (char == '\t') 2 else 1
    }
    return count / 2
}
