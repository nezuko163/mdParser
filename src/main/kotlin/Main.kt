package com.nezuko

import java.util.LinkedList
import java.util.Stack


class ParserMdImpl {

    fun asd(): List<Int> {
        val a = ArrayList<Int>()
        return a
    }

    fun parseMd(text: String): List<MdBlock> {
        val blocks = mutableListOf<MdBlock>()
        val lines = text.lines()

        var isInTable = false
        val tableHeaders = mutableListOf<MdBlock.MdText>()
        val tableRows = mutableListOf<List<MdBlock.MdText>>()
        var isPrevLineEmpty = false

        var i = 0

        while (i < lines.size) {
            val line = lines.get(i)
            val trimmed = line.trim()

            // Пропуск пустых строк
            if (trimmed.isEmpty()) {
                if (isPrevLineEmpty) {
                    blocks.add(MdBlock.MdEmptyLine())
                }
                isPrevLineEmpty = true
            }

            // Заголовки
//            if (trimmed.startsWith("#")) {
//                blocks.add(parseHeader(trimmed))
//            }

            // Изображение
            val imageMatch = Regex("^!\\[.*?]\\((.*?)\\)").find(trimmed)
            if (imageMatch != null) {
                val url = imageMatch.groupValues[1]
                blocks.add(MdBlock.MdImage(url))
                continue
            }

            // Таблицы
            if (trimmed.contains("|")) {
                val columns = trimmed.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                if (!isInTable) {
                    tableHeaders.clear()
                    tableRows.clear()
                    columns.forEach {
                        tableHeaders.add(MdBlock.MdText(it))
                    }
                    isInTable = true
                } else {
                    val row = columns.map { MdBlock.MdText(it) }
                    tableRows.add(row)
                }
                continue
            }

            // Конец таблицы — сохранить блок
            if (isInTable && !trimmed.contains("|")) {
                val map = mutableMapOf<MdBlock.MdText, List<MdBlock.MdText>>()
                for ((i, row) in tableRows.withIndex()) {
                    for ((j, cell) in row.withIndex()) {
                        val key = tableHeaders.getOrNull(j) ?: continue
                        val values = map.getOrDefault(key, emptyList())
                        map[key] = values + cell
                    }
                }
                blocks.add(MdBlock.MdTable(map))
                isInTable = false
            }

            // Просто текст
//            if (!trimmed.contains("|")) {
//                blocks.add(
//                    MdBlock.MdText(
//                        text = trimmed,
//                        italicIndexes = findStyleIndexes(trimmed, "_", "_"),
//                        boldIndexes = findStyleIndexes(trimmed, "**", "**"),
//                        crossedOutIndexes = findStyleIndexes(trimmed, "~~", "~~")
//                    )
//                )
//            }
        }

        // Если таблица осталась незакрытой — добавим
        if (isInTable) {
            val map = mutableMapOf<MdBlock.MdText, List<MdBlock.MdText>>()
            for ((i, row) in tableRows.withIndex()) {
                for ((j, cell) in row.withIndex()) {
                    val key = tableHeaders.getOrNull(j) ?: continue
                    val values = map.getOrDefault(key, emptyList())
                    map[key] = values + cell
                }
            }
            blocks.add(MdBlock.MdTable(map))
        }

        return blocks
    }

    fun parseLines(lines: List<String>, lineNumber: Int): MdBlock.MdText {
        var i = 0
        val line = lines[i]

        while (i < line.length) {

        }
        return MdBlock.MdText(line.substring(i, line.length))
    }

    fun parseText(text: String): List<MdBlock.MdText> {
        return emptyList()
    }

    fun formatText(
        text: String,
        symbols: Map<String, List<Int>>
    ): MdBlock.MdText {
        // Собираем оригинальные диапазоны для стилей
        val italicRanges = mutableListOf<IntArray>()
        val boldRanges = mutableListOf<IntArray>()
        val strikeRanges = mutableListOf<IntArray>()

        for ((sym, positions) in symbols) {
            if (positions.size % 2 != 0) continue // непарное число маркеров
            for (i in positions.indices step 2) {
                val open = positions[i]
                val close = positions[i + 1]
                when (sym) {
                    "***" -> {
                        // жирный+курсив
                        italicRanges.add(intArrayOf(open + sym.length, close))
                        boldRanges.add(intArrayOf(open + sym.length, close))
                    }
                    "**" -> {
                        boldRanges.add(intArrayOf(open + sym.length, close))
                    }
                    "*" -> {
                        italicRanges.add(intArrayOf(open + sym.length, close))
                    }
                    "~~" -> {
                        strikeRanges.add(intArrayOf(open + sym.length, close))
                    }
                }
            }
        }

        // Помечаем позиции маркеров для удаления
        val toRemove = BooleanArray(text.length)
        for ((sym, positions) in symbols) {
            if (positions.size % 2 != 0) continue
            for (i in positions.indices step 2) {
                val open = positions[i]
                val close = positions[i + 1]
                for (j in open until open + sym.length) toRemove[j] = true
                for (j in close until close + sym.length) toRemove[j] = true
            }
        }

        // Строим новый текст без маркеров и карту старых->новых индексов
        val sb = StringBuilder()
        val origToNew = IntArray(text.length)
        var newIdx = 0
        for (i in text.indices) {
            origToNew[i] = newIdx
            if (!toRemove[i]) {
                sb.append(text[i])
                newIdx++
            }
        }

        // Функция для преобразования оригинальных диапазонов в новые
        fun toNewRange(origStart: Int, origEnd: Int): IntArray {
            val start = origToNew[origStart]
            val end = origToNew[origEnd]
            return intArrayOf(start, end)
        }

        val italicFinal = italicRanges.map { toNewRange(it[0], it[1]) }
        val boldFinal = boldRanges.map { toNewRange(it[0], it[1]) }
        val strikeFinal = strikeRanges.map { toNewRange(it[0], it[1]) }

        return MdBlock.MdText(
            text = sb.toString(),
            italicIndexes = italicFinal,
            boldIndexes = boldFinal,
            crossedOutIndexes = strikeFinal,
            header = null
        )
    }



//    fun parseHeader(line: String): MdBlock.MdText {
//        for (level in 0..5) {
//            if (level >= line.length) return MdBlock.MdText("", header = Header.from(level + 1))
//            if (line[level] != '#') {
//                return parseText("").also {
//                    it.header = Header.from(level)
//                }
//            }
//        }
//        return parseText("").also {
//            it.header = Header.from(6)
//        }
//    }

    fun cleanSpaces(text: String): String {
        return text
            .replace(Regex(" {2,}"), " ")
            .trim()
    }

    fun normalizeNewlines(text: String): String {
        return text
            .replace(Regex("(?<!\n)\n(?!\n)"), " ")
            .replace(Regex("\n{2,}"), "\n")
    }
}





