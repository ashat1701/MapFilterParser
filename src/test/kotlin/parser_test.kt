import org.junit.Test
import kotlin.test.assertEquals

class ParserTest {
    @Test
    fun getNumberTestCorrectNumber() {
        val state = ParserState("1", 0)
        val parsed = getNumber(state)
        assertEquals(parsed.type, NodeType.NUMBER)
        assertEquals(parsed.value, "1")
    }
    @Test
    fun getNumberTestIncorrectNumber() {
        val state = ParserState("", 0)
        val parsed = getNumber(state)
        assertEquals(parsed.type, NodeType.UNKNOWN)
        assertEquals(parsed.value, "")
    }
    @Test
    fun getExpressionCorrect() {
        val state = ParserState("(2+3)", 0)
        val parsed = getExpression(state)
        assertEquals(parsed.type, NodeType.OPERATOR)
        assertEquals(parsed.value, "+")
        assertEquals(parsed.children.size, 2)
    }
    @Test
    fun getExpressionIncorrect() {
        val state = ParserState("(2+", 0)
        val parsed = getExpression(state)
        assertEquals(parsed.type, NodeType.UNKNOWN)
    }
    @Test
    fun getExpressionInvalidOperator() {
        val state = ParserState("(2%3)", 0)
        val parsed = getExpression(state)
        assertEquals(parsed.type, NodeType.UNKNOWN)
    }
}

class MiddlewareTest {
    @Test
    fun typeCheckCorrectFilter() {
        val ast = getCall(ParserState("filter{(element>2)}", 0))
        assertEquals(checkTypes(ast), true)
    }
    @Test
    fun typeCheckIncorrectFilter() {
        val ast = getCall(ParserState("filter{(element+2)}", 0))
        assertEquals(checkTypes(ast), false)
    }
    @Test
    fun typeCheckCorrectExpr() {
        val ast = getExpression(ParserState("(((element+2)>3)|(1=2))", 0))
        assertEquals(checkTypes(ast), true)
    }
    @Test
    fun typeCheckIncorrectExpr() {
        val ast = getExpression(ParserState("(((element+2)>3)|(element+2))", 0))
        assertEquals(checkTypes(ast), false)
    }
    @Test
    fun typeCheckIncorrectExprArith() {
        val ast = getExpression(ParserState("(((element+(1>2))>3)|(element>12))", 0))
        assertEquals(checkTypes(ast), false)
    }
}