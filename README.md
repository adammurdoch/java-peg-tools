A Java library to create parsers using Parsing Expression Grammars, or [PEG](https://en.wikipedia.org/wiki/Parsing_expression_grammar). It provides a Java API for describing the parsing expressions and constructing a parser from these expressions.

Includes a sample grammar for parsing Java source.

### TODO

Implementation is in the very early stages, and will scan the input into tokens but does not yet provide a parse tree as output or meaningful error reporting

#### Features

- Construct a parse tree
- Improve error handling and reporting
- Report (line,col) location where parsing stopped
- Report why parsing stopped
- Parse a stream
- Parse byte stream
- Push as well as pull

#### Improvements

- Match as much of the result as possible on failure when speculating (optional, one-or-more, zero-or-more) 
    - Fix case where other alternatives should have been eliminated
- Use immutable positions to represent locations in the stream
- Fix `zeroOrMore(optional(x))`, etc
- Improve matching when there is a common prefix between alternatives

#### Java grammar

Not even slightly complete.

- Accepts more than one `public` or `abstract` modifier on class declaration
