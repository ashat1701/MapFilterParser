# MapFilterParser

Simple parser for Map-Filter syntax.

## Parser

Recursive descent is used for parsing expressions. It can be found in Parser.kt file. Grammar was simple so no lexical analyzer was needed. 
But if there are necessity for lexical analysis, I think standart analyzers like flex would be overkill, so simple lexer can be writeen.

## Type checking

After parsing we get AST so we can check types and optimize expression with different middlewares. 

## Tests 

I wrote simple tests for parser and type checker. They can be found in test directory.
