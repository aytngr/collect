package com.example.domain.ops

import com.example.domain.models.Note
import com.example.domain.models.TextBlock
import java.util.UUID
import kotlin.uuid.Uuid

fun List<TextBlock>.ensureTrailingPlainBlock(): List<TextBlock> {
    val blocks = this.toMutableList()
    if(blocks.isEmpty() || blocks.last() !is TextBlock.PlainText){
        blocks.add(TextBlock.PlainText(id = UUID.randomUUID().toString(), text = ""))
    }

    return blocks.toList()
}

fun insertVoiceBlock(blocks: List<TextBlock>, focusedId: String, text: String, audioFilePath: String, durationMs: Long): List<TextBlock> {
    val blocks = blocks.toMutableList()
    val index = blocks.indexOf(blocks.find { block -> block.id == focusedId })
    val indexToAdd = when (index){
        blocks.size - 1 -> index
        else -> +1
    }

    blocks.add(indexToAdd, TextBlock.VoiceText(id = UUID.randomUUID().toString(), text = text, audioFilePath = audioFilePath, durationMs = durationMs))

    return blocks.toList()
}

fun insertInlineVoiceText(blocks: List<TextBlock>, focusedId: String, text: String, cursor: Int): Triple<List<TextBlock>, String, Int> {
    var merged: String = ""
    var newTextLength: Int = 0

    return Triple(blocks.map { block ->
        if (block.id != focusedId) return@map block

        val existing = when (block) {
            is TextBlock.PlainText -> block.text
            is TextBlock.VoiceText -> block.text
        }

        val safeCursor = cursor.coerceIn(0, existing.length)

        val newText = buildString {
            if (safeCursor > 0 && !existing[safeCursor - 1].isWhitespace()) append(' ')
            append(text)
            if (safeCursor < existing.length && !existing[safeCursor].isWhitespace()) append(' ')
        }
        newTextLength = newText.length
        merged = buildString {
            append(existing, 0, safeCursor)
            append(newText)
            append(existing, safeCursor, existing.length)
        }

        when (block) {
            is TextBlock.PlainText -> block.copy(text = merged)
            is TextBlock.VoiceText -> block.copy(text = merged)
        }
    }, merged, newTextLength)
}


