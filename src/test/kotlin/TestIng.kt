import com.nezuko.Header
import com.nezuko.MdBlock
import com.nezuko.ParserMdImpl
import org.junit.jupiter.api.Test
import org.junit.platform.commons.logging.LoggerFactory
import kotlin.test.Ignore
import kotlin.test.assertEquals

class MdParserTest {
    val log = LoggerFactory.getLogger(javaClass)
    val mdParser = ParserMdImpl()

    @Test
    @Ignore
    fun headerTest() {
        assertEquals(mdParser.parseMd("# 1").first(), MdBlock.MdText("1", header = Header.FIRST))
        assertEquals(mdParser.parseMd("###### 1").first(), MdBlock.MdText("1", header = Header.SIXTH))
        assertEquals(mdParser.parseMd("####### 1").first(), MdBlock.MdText("# 1", header = Header.SIXTH))
    }

    @Test
    @Ignore
    fun paragraphTest() {
        val a = mdParser.parseMd("_1_").first()
        println(a)
        println((a as MdBlock.MdText).italicIndexes.first().contentToString())
        assertEquals(MdBlock.MdText("1", header = Header.FIRST), a)
    }


    @Test
    fun spaceTest() {
        val a = mdParser.cleanSpaces("   aaaa       a   a a a    a   ")
        assertEquals("aaaa a a a a a", a)
    }

    @Test
    fun normalizeTest() {
        val a = mdParser.normalizeNewlines("\n\n\nzxc\nasd\n\n")
        assertEquals("\nzxc asd\n", a)
    }

        @Test
    fun formatTest__bold() {
            var a = hashMapOf(
                Pair("**", listOf(0, 5)),
            )
            var res = mdParser.formatText("**asd**", a)
            log.info { res.toString() }
            log.info { "italic - ${res.italicIndexes.map { it.contentToString() }}" }
            log.info { "bold - ${res.boldIndexes.map { it.contentToString() }}" }
            log.info { "crossed - ${res.crossedOutIndexes.map { it.contentToString() }}" }
            var expected = MdBlock.MdText(
                text = "asd",
                italicIndexes = arrayListOf(),
                boldIndexes = arrayListOf(intArrayOf(0, 3)),
                crossedOutIndexes = arrayListOf(),
                header = null
            )
            assertEquals(expected, res)
    }

    @Test
    fun formatTestItalics() {
        var a = hashMapOf(
            Pair("*", listOf(0, 4)),
        )
        var res = mdParser.formatText("*asd*", a)
        log.info { res.toString() }
        log.info { "italic - ${res.italicIndexes.map { it.contentToString() }}" }
        log.info { "bold - ${res.boldIndexes.map { it.contentToString() }}" }
        log.info { "crossed - ${res.crossedOutIndexes.map { it.contentToString() }}" }
        var expected = MdBlock.MdText(
            text = "asd",
            italicIndexes = arrayListOf(intArrayOf(0, 3)),
            boldIndexes = arrayListOf(),
            crossedOutIndexes = arrayListOf(),
            header = null
        )
        assertEquals(expected, res)
    }

    @Test
    fun formatTestItalicsAndBold() {
        var a = hashMapOf(
            Pair("***", listOf(0, 6)),
        )
        var res = mdParser.formatText("***asd***", a)
        log.info { res.toString() }
        log.info { "italic - ${res.italicIndexes.map { it.contentToString() }}" }
        log.info { "bold - ${res.boldIndexes.map { it.contentToString() }}" }
        log.info { "crossed - ${res.crossedOutIndexes.map { it.contentToString() }}" }
        var expected = MdBlock.MdText(
            text = "asd",
            italicIndexes = arrayListOf(intArrayOf(0, 3)),
            boldIndexes = arrayListOf(intArrayOf(0, 3)),
            crossedOutIndexes = arrayListOf(),
            header = null
        )
        assertEquals(expected, res)
    }

    @Test
    fun formatTestCrossed() {
        var a = hashMapOf(
            Pair("~~", listOf(0, 5)),
        )
        var res = mdParser.formatText("~~asd~~", a)
        log.info { res.toString() }
        log.info { "italic - ${res.italicIndexes.map { it.contentToString() }}" }
        log.info { "bold - ${res.boldIndexes.map { it.contentToString() }}" }
        log.info { "crossed - ${res.crossedOutIndexes.map { it.contentToString() }}" }
        var expected = MdBlock.MdText(
            text = "asd",
            italicIndexes = arrayListOf(),
            boldIndexes = arrayListOf(),
            crossedOutIndexes = arrayListOf(intArrayOf(0, 3)),
            header = null
        )
        assertEquals(expected, res)

    }


    @Test
    fun formatTestCrossedItalicsBold() {
        var a = hashMapOf(
            Pair("~~", listOf(0, 11)),
            Pair("***", listOf(2, 8)),
        )
        var res = mdParser.formatText("~~***asd***~~", a)
        log.info { res.toString() }
        log.info { "italic - ${res.italicIndexes.map { it.contentToString() }}" }
        log.info { "bold - ${res.boldIndexes.map { it.contentToString() }}" }
        log.info { "crossed - ${res.crossedOutIndexes.map { it.contentToString() }}" }
        var expected = MdBlock.MdText(
            text = "asd",
            italicIndexes = arrayListOf(intArrayOf(0, 3)),
            boldIndexes = arrayListOf(intArrayOf(0, 3)),
            crossedOutIndexes = arrayListOf(intArrayOf(0, 3)),
            header = null
        )
        assertEquals(expected, res)
    }


    @Test
    fun formatTestCrossedItalicsBoldWithSpecSymb() {
        var a = hashMapOf(
            Pair("***", listOf(3, 9)),
            Pair("~~", listOf(0, 12)),
        )


        var res = mdParser.formatText("~~~***asd***~~", a)
        log.info { res.toString() }
        log.info { "italic - ${res.italicIndexes.map { it.contentToString() }}" }
        log.info { "bold - ${res.boldIndexes.map { it.contentToString() }}" }
        log.info { "crossed - ${res.crossedOutIndexes.map { it.contentToString() }}" }

        var expected = MdBlock.MdText(
            text = "~asd",
            italicIndexes = arrayListOf(intArrayOf(1, 4)),
            boldIndexes = arrayListOf(intArrayOf(1, 4)),
            crossedOutIndexes = arrayListOf(intArrayOf(0, 4)),
            header = null
        )
        assertEquals(expected, res)
    }
}