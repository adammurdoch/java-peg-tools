A Java library to create parsers using Parsing Expression Grammars, or [PEG](https://en.wikipedia.org/wiki/Parsing_expression_grammar). It provides a Java API for describing the parsing expressions and constructing a parser from this.

Includes a sample grammar for parsing Java.

### TODO

Implementation is in the very early stages, and doesn't do anything much.

- Don't create a string for each match, collect locations instead
- Detangle producing the result (eg grouping) from matching
- Match as much as possible on failure when speculating 
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
