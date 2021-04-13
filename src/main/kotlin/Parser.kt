enum class NodeType {
    OPERATOR, NUMBER, MAP, FILTER, ELEMENT, UNKNOWN
}
enum class ErrorType {
    UNKNOWN_CALL, ERROR, MISSING_BRACKET, EXPECTED_OPERATOR,
    EXPECTED_NUMBER, SEQ_ERROR, EXPECTED_EXPRESSION, EXPECTED_BOOLEAN,
    EXPECTED_NUMERIC
}
val TOKENS = mapOf(
    NodeType.MAP to "map",
    NodeType.FILTER to "filter",
    NodeType.ELEMENT to "element"
)

val BOOLEAN = listOf(
    '&', '|'
)

val COMPARE = listOf(
    '>', '<', '='
)

val ARITHMETIC = listOf(
    '+', '-', '*'
)

val OPERATORS = listOf(
    BOOLEAN, COMPARE, ARITHMETIC
).flatten()

data class ASTNode(
    var type: NodeType = NodeType.UNKNOWN,
    var value: String = "",
    var parent: ASTNode? = null,
    var children: MutableList<ASTNode> = mutableListOf()
) {
    override fun toString(): String {
        var result = ""
        if (type == NodeType.FILTER || type == NodeType.MAP) {
            result += TOKENS[type]!! + "{"
            result += children[0].toString()
            result += "}"
            return result
        }
        if (type == NodeType.ELEMENT) {
            return TOKENS[type]!!
        }
        if (type == NodeType.OPERATOR) {
            var isBracketNeeded = false
            if (parent != null && parent!!.type == NodeType.OPERATOR ) {
                result += "("
                isBracketNeeded = true
            }
            for (child in children) {
                result += child.toString()
                result += value
            }
            result = result.dropLast(1)
            if (isBracketNeeded)
                result += ")"
            return result
        }
        return value
    }
}

class ParserState(val str: String, var pos:Int)

fun printError(e:ErrorType, state: ParserState) {
    print("[ERROR] at ${state.pos + 1}: ")
    when(e) {
        ErrorType.UNKNOWN_CALL -> println("Wrong call")
        ErrorType.MISSING_BRACKET -> println("Expected closing bracket")
        ErrorType.EXPECTED_NUMBER -> println("Expected number")
        ErrorType.EXPECTED_OPERATOR -> println("Invalid operator")
        ErrorType.EXPECTED_EXPRESSION -> println("Expected expression")
        ErrorType.SEQ_ERROR -> println("Couldn't split into calls")
        ErrorType.EXPECTED_BOOLEAN -> println("Type error: Expected boolean type")
        ErrorType.EXPECTED_NUMERIC -> println("Type error: Expected numeric type")
        else -> println("Syntax error")
    }
}

fun getCallType(state: ParserState): NodeType {
    if (TOKENS[NodeType.MAP]?.let { state.str.startsWith(it, state.pos) } == true) {
        return NodeType.MAP
    }
    if (TOKENS[NodeType.FILTER]?.let { state.str.startsWith(it, state.pos) } == true) {
        return NodeType.FILTER
    }
    printError(ErrorType.UNKNOWN_CALL, state)
    return NodeType.UNKNOWN
}

fun getExpressionType(state: ParserState): NodeType {
    if (state.str.startsWith("element", state.pos)) {
        return NodeType.ELEMENT
    }
    return NodeType.UNKNOWN
}

fun checkOperator(state: ParserState):Boolean {
    return OPERATORS.contains(state.str[state.pos])
}

fun getNumber(state: ParserState): ASTNode {
    val result = ASTNode()
    if (state.pos >= state.str.length) {
        result.type = NodeType.UNKNOWN
        printError(ErrorType.EXPECTED_NUMBER, state)
        return result
    }
    result.type = NodeType.NUMBER
    if (state.str[state.pos] == '-') {
        result.value += "-"
        state.pos++
    }
    while (state.pos < state.str.length && state.str[state.pos].isDigit()) {
        result.value += state.str[state.pos]
        state.pos++
    }
    if (result.value == "" || result.value == "-")  {
        printError(ErrorType.EXPECTED_NUMBER, state)
        result.type = NodeType.UNKNOWN
    }
    return result
}


fun getExpression(state: ParserState): ASTNode {
    val result = ASTNode()
    result.type = getExpressionType(state)
    if (result.type != NodeType.UNKNOWN) {
        state.pos += TOKENS[result.type]?.length!!
        return result
    }
    if (state.pos >= state.str.length) {
        result.type = NodeType.UNKNOWN
        printError(ErrorType.EXPECTED_EXPRESSION, state)
        return result
    }
    if (state.str[state.pos] == '(') {
        state.pos++
        val leftChild = getExpression(state)
        if (!checkOperator(state)) {
            printError(ErrorType.EXPECTED_OPERATOR, state)
            result.type = NodeType.UNKNOWN
        } else {
            result.type = NodeType.OPERATOR
            result.value = state.str[state.pos].toString()
        }
        state.pos++
        val rightChild = getExpression(state)
        if (state.str.getOrNull(state.pos) != ')') {
            printError(ErrorType.MISSING_BRACKET, state)
            result.type = NodeType.UNKNOWN
        }
        else
            state.pos++
        leftChild.parent = result
        rightChild.parent = result
        if (leftChild.type == NodeType.UNKNOWN || rightChild.type == NodeType.UNKNOWN)
            result.type = NodeType.UNKNOWN
        result.children.add(leftChild)
        result.children.add(rightChild)

        return result
    }
    return getNumber(state)
}

fun getCall(state: ParserState): ASTNode {
    val result = ASTNode()
    result.type = getCallType(state)
    state.pos += TOKENS[result.type]?.length!!
    if (state.str[state.pos] != '{')
        printError(ErrorType.ERROR, state)
    else
        state.pos++
    val child = getExpression(state)
    child.parent = result
    result.children.add(child)
    return result
}