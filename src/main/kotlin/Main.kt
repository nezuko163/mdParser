package com.nezuko


class ParserMdImpl {

    fun asd(): List<Int> {
        val a = ArrayList<Int>()
        return a
    }

    fun parseMd(text: String): List<MdBlock> {
        val imageRegex = Regex("^!\\[[^\\]]*]\\(([^)]+)\\)")

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
                i++
                continue
            }

            // Заголовки
//            if (trimmed.startsWith("#")) {
//                blocks.add(parseHeader(trimmed))
//            }

            // Изображение
            if (trimmed.length < 500) {
                val imageMatch = imageRegex.find(trimmed)
                if (imageMatch != null) {
                    val url = imageMatch.groupValues[1]
                    blocks.add(MdBlock.MdImage(url))
                    i++
                    continue
                }
            }

            // Таблицы
            if (trimmed.contains("|")) {
                val columns = trimmed.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                if (!isInTable) {
                    tableHeaders.clear()
                    tableRows.clear()
                    columns.forEach {
                        tableHeaders.add(parseText(it))
                    }
                    isInTable = true
                } else {
                    val row = columns.map { parseText(it) }
                    tableRows.add(row)
                }
                i++
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
            i++
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

    fun parseText(text: String): MdBlock.MdText {
        val symbols = linkedMapOf<String, MutableList<Int>>()
        val markers = listOf("~~~", "***", "~~", "**", "*", "~")
        var i = 0
        while (i < text.length) {
            var matched = false
            for (marker in markers) {
                if (text.startsWith(marker, i)) {
                    symbols.getOrPut(marker) { mutableListOf() }.add(i)
                    i += marker.length
                    matched = true
                    break
                }
            }
            if (!matched) {
                i++
            }
        }
        val symbolMap: Map<String, List<Int>> = symbols.mapValues { it.value.sorted() }
        return formatText(text, symbolMap)
    }

    fun formatText(
        text: String,
        symbols: Map<String, List<Int>>
    ): MdBlock.MdText {
        val italicRanges = mutableListOf<IntArray>()
        val boldRanges = mutableListOf<IntArray>()
        val strikeRanges = mutableListOf<IntArray>()

        for ((sym, positions) in symbols) {
            if (positions.size % 2 != 0) continue
            for (j in positions.indices step 2) {
                val open = positions[j]
                val close = positions[j + 1]
                when (sym) {
                    "***", "~~~" -> {
                        italicRanges += intArrayOf(open + sym.length, close)
                        boldRanges += intArrayOf(open + sym.length, close)
                    }
                    "**" -> {
                        boldRanges += intArrayOf(open + sym.length, close)
                    }
                    "*" -> {
                        italicRanges += intArrayOf(open + sym.length, close)
                    }
                    "~~" -> {
                        strikeRanges += intArrayOf(open + sym.length, close)
                    }
                    "~" -> {
                        strikeRanges += intArrayOf(open + sym.length, close)
                    }
                }
            }
        }

        val toRemove = BooleanArray(text.length)
        for ((sym, positions) in symbols) {
            if (positions.size % 2 != 0) continue
            for (j in positions.indices step 2) {
                val open = positions[j]
                val close = positions[j + 1]
                for (k in open until open + sym.length) toRemove[k] = true
                for (k in close until close + sym.length) toRemove[k] = true
            }
        }

        val sb = StringBuilder()
        val origToNew = IntArray(text.length)
        var newIdx = 0
        for (idx in text.indices) {
            origToNew[idx] = newIdx
            if (!toRemove[idx]) {
                sb.append(text[idx])
                newIdx++
            }
        }

        fun toNew(orig: Int, end: Int): IntArray {
            return intArrayOf(origToNew[orig], origToNew[end])
        }

        val italicFinal = italicRanges.map { toNew(it[0], it[1]) }
        val boldFinal = boldRanges.map { toNew(it[0], it[1]) }
        val strikeFinal = strikeRanges.map { toNew(it[0], it[1]) }

        return MdBlock.MdText(
            text = sb.toString(),
            italicIndexes = italicFinal,
            boldIndexes = boldFinal,
            crossedOutIndexes = strikeFinal,
            header = null
        )
    }




    fun parseHeader(line: String): MdBlock.MdText {
        for (level in 0..5) {
            if (level >= line.length) return MdBlock.MdText("", header = Header.from(level + 1))
            if (line[level] != '#') {
                return parseText("").also {
                    it.header = Header.from(level)
                }
            }
        }
        return parseText("").also {
            it.header = Header.from(6)
        }
    }

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


fun main() {
    // Тестовая Markdown таблица с разными функциями
    val testMd = """
        | **Language** | *Rating* | ~~Experience~~ | Extras     |
        |--------------|----------|----------------|------------|
        | Kotlin       | 5        | 4 years        | Awesome!   |
        | Java         | 4        | ~~10 years~~   | Legacy     |
        | _Python_     | 5        | 6 years        | **Popular**|
        | Rust         | ~~3~~    | 2 years        |            |
    """.trimIndent()

    val parser = ParserMdImpl()
    val result = parser.parseMd(testMd)

    println("=".repeat(50))
    println("РАСПАРСЕННЫЕ БЛОКИ:")
    println("=".repeat(50))

    result.forEachIndexed { index, block ->
        println("\nБлок #${index + 1}: ${block::class.simpleName}")

        when (block) {
            is MdBlock.MdTable -> {
                println("\nТАБЛИЦА:")
                println("-".repeat(50))

                // Вывод заголовков
                println("ЗАГОЛОВКИ:")
                block.content.keys.forEach { header ->
                    printHeader(header)
                }

                // Вывод данных
                println("\nДАННЫЕ:")
                val rows = transposeTable(block.content)
                rows.forEachIndexed { rowIndex, row ->
                    println("\nСтрока #${rowIndex + 1}:")
                    row.forEach { cell ->
                        printCell(cell)
                    }
                }
            }
            else -> println(block)
        }
    }
}

// Вспомогательные функции для красивого вывода
fun printHeader(header: MdBlock.MdText) {
    val styles = listOf(
        "ЖИРНЫЙ" to header.boldIndexes.isNotEmpty(),
        "КУРСИВ" to header.italicIndexes.isNotEmpty(),
        "ЗАЧЕРК" to header.crossedOutIndexes.isNotEmpty()
    ).filter { it.second }.map { it.first }

    val styleInfo = if (styles.isNotEmpty()) " [${styles.joinToString()}]" else ""
    println("  - ${header.text}$styleInfo")
}

fun printCell(cell: MdBlock.MdText) {
    val styles = mutableListOf<String>()
    if (cell.boldIndexes.isNotEmpty()) styles.add("ЖИРНЫЙ")
    if (cell.italicIndexes.isNotEmpty()) styles.add("КУРСИВ")
    if (cell.crossedOutIndexes.isNotEmpty()) styles.add("ЗАЧЕРК")

    val styleInfo = if (styles.isNotEmpty()) " [${styles.joinToString()}]" else ""
    println("  - ${cell.text}${styleInfo}")
}

// Транспонируем таблицу для удобного вывода по строкам
fun transposeTable(table: Map<MdBlock.MdText, List<MdBlock.MdText>>): List<List<MdBlock.MdText>> {
    val rows = mutableListOf<List<MdBlock.MdText>>()
    val maxRows = table.values.maxOfOrNull { it.size } ?: 0

    for (i in 0 until maxRows) {
        val row = mutableListOf<MdBlock.MdText>()
        table.values.forEach { column ->
            row.add(column.getOrElse(i) { MdBlock.MdText("") })
        }
        rows.add(row)
    }

    return rows
}


