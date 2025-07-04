import com.nezuko.Header
import com.nezuko.MdBlock
import com.nezuko.ParserMdImpl
import org.junit.jupiter.api.Test
import org.junit.platform.commons.logging.LoggerFactory
import kotlin.test.assertEquals

class MdParserTest {
    val log = LoggerFactory.getLogger(javaClass)
    val mdParser = ParserMdImpl()
    @Test
    fun headerTest() {
        assertEquals(mdParser.parseMd("# 1").first(), MdBlock.MdText("1", header = Header.FIRST))
        assertEquals(mdParser.parseMd("###### 1").first(), MdBlock.MdText("1", header = Header.SIXTH))
        assertEquals(mdParser.parseMd("####### 1").first(), MdBlock.MdText("# 1", header = Header.SIXTH))
    }

    @Test
    fun paragraphTest() {
        val a = mdParser.parseMd("_1_").first()
        println(a)
//        println((a as MdBlock.MdText).italicIndexes.first().contentToString())
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
        var a = ArrayDeque<Pair<Int, String>>()
        a.add(Pair(0, "**"))
        a.add(Pair(5, "**"))
        var res = mdParser.formatText("**asd**", a)
        var expected = MdBlock.MdText(text="asd", italic=false, bold=true, crossedOut=false, header=null)
        assertEquals(expected, res)
    }

    @Test
    fun formatTestItalics() {
        var a = ArrayDeque<Pair<Int, String>>()
        a.add(Pair(0, "*"))
        a.add(Pair(4, "*"))
        var res = mdParser.formatText("*asd*", a)
        var expected = MdBlock.MdText(text = "asd", italic = true, bold = false, crossedOut = false, header = null)
        assertEquals(expected, res)
    }
    @Test
    fun formatTestItalicsAndBold() {
        var a = ArrayDeque<Pair<Int, String>>()
        a.add(Pair(0, "**"))
        a.add(Pair(2, "*"))
        a.add(Pair(6, "*"))
        a.add(Pair(7, "**"))
        var res = mdParser.formatText("***asd***", a)
        var expected = MdBlock.MdText(text = "asd", italic = true, bold = true, crossedOut = false, header = null)
        assertEquals(expected, res)
    }
    @Test
    fun formatTestCrossed() {
        var a = ArrayDeque<Pair<Int, String>>()
        a.add(Pair(0, "~"))
        a.add(Pair(4, "~"))
        var res = mdParser.formatText("~asd~", a)
        var expected = MdBlock.MdText(text = "asd", italic = false, bold = false, crossedOut = true, header = null)
        assertEquals(expected, res)
    }


    @Test
    fun formatTestCrossedItalicsBold() {
        var a = ArrayDeque<Pair<Int, String>>()
        a.add(Pair(0, "~"))
        a.add(Pair(1, "**"))
        a.add(Pair(3, "*"))
        a.add(Pair(7, "*"))
        a.add(Pair(8, "**"))
        a.add(Pair(10, "~"))
        var res = mdParser.formatText("~***asd***~", a)
        var expected = MdBlock.MdText(text = "asd", italic = true, bold = true, crossedOut = true, header = null)
        assertEquals(expected, res)
    }


    @Test
    fun formatTestCrossedItalicsBoldWithSpecSymb() {
        var a = ArrayDeque<Pair<Int, String>>()
        a.add(Pair(0, "~"))
        a.add(Pair(1, "~"))
        a.add(Pair(2, "**"))
        a.add(Pair(4, "*"))
        a.add(Pair(8, "*"))
        a.add(Pair(9, "**"))
        a.add(Pair(11, "~"))
        var res = mdParser.formatText("~~***asd***~", a)
        var expected = MdBlock.MdText(text = "~asd", italic = true, bold = true, crossedOut = true, header = null)
        assertEquals(expected, res)
    }



}