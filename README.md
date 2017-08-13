A Java library to create parsers using Parsing Expression Grammars, or [PEG](https://en.wikipedia.org/wiki/Parsing_expression_grammar). It provides a Java API for describing the parsing expressions and constructing a parser from these expressions.

Includes a sample grammar for parsing Java.

### TODO

Implementation is in the very early stages, and doesn't do anything much.

- Match as much of the result as possible on failure when speculating (optional, one-or-more, zero-or-more) 
- Use immutable positions to represent locations in the stream
- Report (line,col) location where parsing stopped
- Report why parsing stopped
- Parse failures
- Fix `zeroOrMore(optional(x))`, etc
- Parse a stream
- Parse byte stream
- Push as well as pull
- Construct a parse tree
- Java grammar
    - Not even slightly complete.
    - Accepts more than one `public` or `abstract` modifier on class declaration
