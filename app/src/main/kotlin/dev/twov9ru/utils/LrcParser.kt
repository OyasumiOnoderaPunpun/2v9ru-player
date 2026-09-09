package dev.twov9ru.utils

import java.io.File

data class LrcLine(val timeMs: Long, val text: String)

object LrcParser {
    // Matches standard LRC time tags like [01:23.45] or [01:23.456]
    private val TIME_TAG_REGEX = Regex("""\[(\d{2,}):(\d{2})(?:[.:](\d{2,3}))?]""")

    fun parse(file: File): List<LrcLine> {
        if (!file.exists() || !file.canRead()) return emptyList()
        return parseLines(file.readLines())
    }

    fun parseLines(lines: List<String>): List<LrcLine> {
        val lrcLines = mutableListOf<LrcLine>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // A single line can have multiple time tags: [01:23.45][02:34.56]Lyrics text
            val matches = TIME_TAG_REGEX.findAll(trimmed).toList()
            if (matches.isEmpty()) continue

            // The text is whatever is after the last time tag
            val textStart = matches.last().range.last + 1
            val text = trimmed.substring(textStart).trim()

            for (match in matches) {
                val min = match.groupValues[1].toLong()
                val sec = match.groupValues[2].toLong()
                val millisStr = match.groupValues[3]
                // If it's 2 digits, it's hundredths of a second. If 3, it's thousandths.
                val millis = if (millisStr.length == 2) millisStr.toLong() * 10 else millisStr.toLong()
                
                val timeMs = min * 60 * 1000 + sec * 1000 + millis
                lrcLines.add(LrcLine(timeMs, text))
            }
        }

        // Sort by time in case multi-tags were out of order
        return lrcLines.sortedBy { it.timeMs }
    }
}
