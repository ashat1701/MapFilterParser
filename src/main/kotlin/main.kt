import java.beans.Expression

fun makeCopy(what:ASTNode): ASTNode {
    val result = ASTNode()
    result.type = what.type
    result.value = "" + what.value
    for (child in what.children) {
        val childCopy = makeCopy(child)
        childCopy.parent = result
        result.children.add(childCopy)
    }
    return result
}

fun elementSubstitute(where: ASTNode, element:ASTNode):ASTNode {
    if (where.type == NodeType.ELEMENT) {
        return makeCopy(element)
    }
    val result = ASTNode()
    result.type = where.type
    result.value = "" + where.value
    for (child in where.children) {
        val newChild = elementSubstitute(child, element)
        newChild.parent = result
        result.children.add(newChild)
    }
    return result
}

fun addWithOperator(op: Char, where: ASTNode, what:ASTNode):ASTNode {
    val result = ASTNode()
    result.type = NodeType.OPERATOR
    result.value = op.toString()
    result.children.add(where)
    result.children.add(what)
    return result
}

fun processMapCall(prevMap: ASTNode, curMap: ASTNode):ASTNode {
    return elementSubstitute(curMap, prevMap)
}

fun processFilterCall(curMap: ASTNode, prevFilter: ASTNode, curFilter: ASTNode) : ASTNode {
    val newFilter = elementSubstitute(curFilter, curMap)
    return addWithOperator('&', prevFilter, newFilter)
}

fun getExprFromCall(call: ASTNode) : ASTNode{
    require(call.children.size > 0) {
        "Type error: Expression missing in call"
    }
    return call.children[0]
}

fun splitIntoCalls(callChain : String?): List<String>? {
     return callChain?.split("%>%".toRegex())
}

fun createCall(type:NodeType,expr: ASTNode): ASTNode {
    val result = ASTNode()
    result.type = type
    result.children.add(expr)
    return result
}

fun main() {
    val s = readLine()
    val calls = splitIntoCalls(s)
    if (calls != null) {
        var filterExpr = ASTNode()
        var mapExpr = ASTNode(NodeType.ELEMENT)
        for (call in calls) {
            val parser = ParserState(call, 0)
            val callAst = getCall(parser)
            checkTypes(callAst)
            when (callAst.type) {
                NodeType.FILTER -> {
                    filterExpr = if (filterExpr.type == NodeType.UNKNOWN) {
                        elementSubstitute(getExprFromCall(callAst), mapExpr)
                    } else {
                        processFilterCall(mapExpr, filterExpr, getExprFromCall(callAst))
                    }
                }
                NodeType.MAP -> {
                    mapExpr = processMapCall(mapExpr, getExprFromCall(callAst))
                }
                else -> break
            }
        }

        print(createCall(NodeType.FILTER, filterExpr).toString() + "%>%")
        println(createCall(NodeType.MAP, mapExpr).toString())
    } else {
        printError(ErrorType.SEQ_ERROR, ParserState(
            "", 0
        ))
        return
    }

}

