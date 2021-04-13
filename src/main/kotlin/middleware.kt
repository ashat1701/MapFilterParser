fun isBoolean(node: ASTNode): Boolean {
    if (node.type == NodeType.OPERATOR && BOOLEAN.contains(node.value[0]))
        return true
    if (node.type == NodeType.OPERATOR && COMPARE.contains(node.value[0]))
        return true
    return false
}

fun isNumeric(node: ASTNode): Boolean {
    if (node.type == NodeType.ELEMENT || node.type == NodeType.NUMBER)
        return true
    if (node.type == NodeType.OPERATOR && ARITHMETIC.contains(node.value[0]))
        return true
    return false
}

fun checkTypes(node: ASTNode): Boolean {
    for (child in node.children) {
        if (!checkTypes(child))
            return false
    }
    if (node.type == NodeType.OPERATOR) {
        if (BOOLEAN.contains(node.value[0])) {
            if (!isBoolean(node.children[0]) || !isBoolean(node.children[1])) {
                printError(ErrorType.EXPECTED_BOOLEAN, ParserState("", 0))
                return false
            }
        }
        if (ARITHMETIC.contains(node.value[0])) {
            if (!isNumeric(node.children[0]) || !isNumeric(node.children[1])) {
                printError(ErrorType.EXPECTED_NUMERIC,ParserState("", 0))
                return false
            }
        }
        if (COMPARE.contains(node.value[0])) {
            if (!isNumeric(node.children[0]) || !isNumeric(node.children[1])) {
                printError(ErrorType.EXPECTED_NUMERIC,ParserState("", 0))
                return false
            }
        }
    }
    if (node.type == NodeType.MAP) {
        if (!isNumeric(node.children[0])) {
            printError(ErrorType.EXPECTED_NUMERIC,ParserState("", 0))
            return false
        }
    }
    if (node.type == NodeType.FILTER) {
        if (!isBoolean(node.children[0])) {
            printError(ErrorType.EXPECTED_BOOLEAN, ParserState("", 0))
            return false
        }
    }
    return true
}