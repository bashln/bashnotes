package com.offlinenotes.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class SyntaxHighlightingTransformation(
    private val isOrg: Boolean,
    private val listMarkerColor: Color,
    private val codeTextColor: Color,
    private val codeDelimiterColor: Color
) : VisualTransformation {

    private val markdownListPrefixRegex =
        Regex("^\\s*(?:[-*+]\\s(?:\\[[ xX]\\]\\s)?|\\d+\\.\\s)", RegexOption.MULTILINE)
    private val orgListPrefixRegex =
        Regex("^\\s*(?:[-+]\\s|\\d+[.)]\\s)", RegexOption.MULTILINE)

    private val markdownCodeDelimiterRegex = Regex("^\\s*```.*$", RegexOption.MULTILINE)
    private val orgCodeDelimiterRegex = Regex(
        "^\\s*#\\+(begin_src|end_src)\\b.*$",
        setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = buildAnnotatedString {
            append(text)
            applySyntaxStyles(text.text)
        }
        return TransformedText(highlighted, OffsetMapping.Identity)
    }

    private fun AnnotatedString.Builder.applySyntaxStyles(content: String) {
        if (content.isEmpty()) return

        val blocks = if (isOrg) collectOrgCodeBlocks(content) else collectMarkdownCodeBlocks(content)
        blocks.delimiterRanges.forEach { range ->
            addStyle(codeDelimiterStyle(), range.start, range.endExclusive)
        }
        blocks.contentRanges.forEach { range ->
            addStyle(codeStyle(), range.start, range.endExclusive)
        }

        val listRegex = if (isOrg) orgListPrefixRegex else markdownListPrefixRegex
        val exclusionRanges = mergeRanges(blocks.delimiterRanges + blocks.contentRanges)
        var exclusionIndex = 0

        listRegex.findAll(content).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1

            while (exclusionIndex < exclusionRanges.size && exclusionRanges[exclusionIndex].endExclusive <= start) {
                exclusionIndex++
            }

            val overlapsExcludedRange =
                exclusionIndex < exclusionRanges.size &&
                    exclusionRanges[exclusionIndex].start < end &&
                    start < exclusionRanges[exclusionIndex].endExclusive

            if (!overlapsExcludedRange) {
                addStyle(SpanStyle(color = listMarkerColor), start, end)
            }
        }
    }

    private fun collectMarkdownCodeBlocks(content: String): CodeBlockRanges {
        val delimiters = markdownCodeDelimiterRegex.findAll(content)
            .map { match -> TextRange(match.range.first, match.range.last + 1) }
            .toList()

        val contentRanges = mutableListOf<TextRange>()
        var openDelimiter: TextRange? = null
        delimiters.forEach { delimiter ->
            val currentOpen = openDelimiter
            if (currentOpen == null) {
                openDelimiter = delimiter
                return@forEach
            }

            val start = rangeAfterLineBreak(content, currentOpen.endExclusive)
            val end = rangeBeforeLineBreak(content, delimiter.start)
            if (start < end) {
                contentRanges.add(TextRange(start, end))
            }
            openDelimiter = null
        }

        return CodeBlockRanges(delimiterRanges = delimiters, contentRanges = contentRanges)
    }

    private fun collectOrgCodeBlocks(content: String): CodeBlockRanges {
        val delimiters = mutableListOf<TextRange>()
        val contentRanges = mutableListOf<TextRange>()
        var openDelimiter: TextRange? = null

        orgCodeDelimiterRegex.findAll(content).forEach { match ->
            val delimiterRange = TextRange(match.range.first, match.range.last + 1)
            delimiters.add(delimiterRange)

            val type = match.groupValues[1].lowercase()
            if (type == "begin_src") {
                if (openDelimiter == null) {
                    openDelimiter = delimiterRange
                }
                return@forEach
            }

            val currentOpen = openDelimiter
            if (currentOpen != null) {
                val start = rangeAfterLineBreak(content, currentOpen.endExclusive)
                val end = rangeBeforeLineBreak(content, delimiterRange.start)
                if (start < end) {
                    contentRanges.add(TextRange(start, end))
                }
                openDelimiter = null
            }
        }

        return CodeBlockRanges(delimiterRanges = delimiters, contentRanges = contentRanges)
    }

    private fun mergeRanges(ranges: List<TextRange>): List<TextRange> {
        if (ranges.isEmpty()) return emptyList()

        val sorted = ranges.sortedBy { it.start }
        val merged = mutableListOf<TextRange>()
        var current = sorted.first()

        for (index in 1 until sorted.size) {
            val next = sorted[index]
            if (next.start <= current.endExclusive) {
                current = TextRange(current.start, maxOf(current.endExclusive, next.endExclusive))
            } else {
                merged.add(current)
                current = next
            }
        }

        merged.add(current)
        return merged
    }

    private fun rangeAfterLineBreak(content: String, index: Int): Int {
        return if (index < content.length && content[index] == '\n') index + 1 else index
    }

    private fun rangeBeforeLineBreak(content: String, index: Int): Int {
        if (index <= 0) return index
        return if (content[index - 1] == '\n') index - 1 else index
    }

    private fun codeStyle(): SpanStyle {
        return SpanStyle(
            color = codeTextColor,
            fontFamily = FontFamily.Monospace
        )
    }

    private fun codeDelimiterStyle(): SpanStyle {
        return SpanStyle(
            color = codeDelimiterColor,
            fontFamily = FontFamily.Monospace
        )
    }

    private data class CodeBlockRanges(
        val delimiterRanges: List<TextRange>,
        val contentRanges: List<TextRange>
    )

    private data class TextRange(
        val start: Int,
        val endExclusive: Int
    )
}
