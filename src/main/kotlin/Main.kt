package com.nezuko



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
        val phrase = StringBuilder()

        val symbols = ArrayDeque<Pair<Int, String>>()
        var startPhrase = -1
        val res = arrayListOf<MdBlock.MdText>()
        var starCount = 0

        if (text[0] == '*') {
            startPhrase = 0
            starCount++
        } else if (text[0] == '~') {
            startPhrase = 0
            symbols.add(Pair(0, "~"))
        } else {
            phrase.append(text)
        }

        var i = 1

        while (i < text.length) {
            val c = text[i]
            if (c == '*') {
                starCount++
                if (startPhrase == -1) {
                    startPhrase = i
                }
                if (starCount == 3) {
                    symbols.add(Pair(i - 2, "***"))
                    starCount = 0
                }

            } else if (c == '~') {
                if (starCount != 0) {
                    symbols.add(Pair(i - starCount - 1, "*".repeat(starCount)))
                    starCount = 0
                }
                if (startPhrase == -1) {
                    startPhrase = i
                }
                symbols.add(Pair(i, "~"))
            } else {
                if (starCount != 0) {
                    symbols.add(Pair(i - starCount - 1, "*".repeat(starCount)))
                    starCount = 0
                }
                if (startPhrase != -1 && (text[i - 1] == '*' || text[i - 1] == '~')) {
                    res.add(formatText(phrase.toString(), symbols))
                    startPhrase = -1
                }
            }
            i++
            phrase.append(c)

        }
        return res
    }

    fun formatText(
        text: String,
        symbols: ArrayDeque<Pair<Int, String>>,
    ): MdBlock.MdText {
        println(text)
        println(symbols)
        var left = 0
        var right = symbols.size - 1

        var italic = false
        var bold = false
        var crossedOut = false
        var start = 0
        var end = right
        while (left < right) {
            val (leftPos, lSymb) = symbols.removeFirst()
            val (rightPos, rSymb) = symbols.removeLast()
            if (lSymb == rSymb) {
                when (lSymb) {
                    "***" -> {
                        italic = true
                        bold = true
                    }
                    "**" -> bold = true
                    "*" -> italic = true
                    "~" -> crossedOut = true
                }
                start = leftPos + lSymb.length
                end = rightPos
                left++
                right--
            } else {
                right--
            }
            println("start = $start; end = $end")
        }
        return MdBlock.MdText(
            text = text.substring(start, end),
            italic = italic,
            bold = bold,
            crossedOut = crossedOut,
        )

    }
    fun formatText(text: String): MdBlock.MdText {
        var startPhrase = -1
        var starCount = 0
        var hhCount = 0

        var i = 0

        while (i < text.length) {
            val c = text[i]
            if (c == '*') {
                starCount++
            }
        }
    }
    fun parseGroup(group: String): MdBlock.MdText {
        val
    }

    fun parseGroup1(group: String): MdBlock.MdText {
        val start = group[0]
        val startIsStar = start == '*'
        var startCount = 0

        var italic = false
        var bold = false
        var crossedOut = false

        for (i in 0..2) {
            if (group[i] == start) {
                startCount++
            } else {
                break
            }
        }

        var i = startCount
        while (i < group.length) {
            if (group[i] == start) {
                var flag = true
                for (j in 1..startCount) {
                    i++
                    if (group[i] != start) {
                        flag = false
                        break
                    }
                }
                if (flag) {
                    when (group[i].toString().repeat(startCount)) {
                        "***" -> {
                            italic = true
                            bold = true
                        }
                        "**" -> bold = true
                        "*" -> italic = true
                        "~~" -> crossedOut = true
                    }
                    return MdBlock.MdText(group.substring(startCount - 1, i),
                        italic, bold, crossedOut)
                }
            }
        }
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





