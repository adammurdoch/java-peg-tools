A Java library to create parsers using Parsing Expression Grammars, or [PEG](https://en.wikipedia.org/wiki/Parsing_expression_grammar). It provides a Java API for describing the parsing expressions and constructing a parser from these expressions.

Includes a sample grammar for parsing Java source.

### TODO

The implementation is in the very early stages and can scan the input into tokens, but does not yet provide a parse tree as output or meaningful error reporting.

#### Missing features

- Construct a parse tree
- Improve error handling and reporting
- Report (line,col) location where parsing stopped
- Report why parsing stopped
- Parse a stream
- Parse byte stream
- Push as well as pull

#### Fixes

- Match as much of the result as possible on failure when speculating (optional, one-or-more, zero-or-more) 
    - Fix case where other alternatives should have been eliminated
    - test: A? B where A partially matched, B no match
    - test: A? B where A partially matched and B partially matched
    - test: A* B where A partially matched zero or more times, B no match
    - test: A* B where A partially matched zero or more times, B partially matched
    - test: A+ B where A partially matched zero or more times, B no match
    - test: A+ B where A partially matched zero or more times, B partially matched
- Use immutable positions to represent locations in the stream
- Fix `zeroOrMore(optional(x))`, etc
- Improve matching when there is a common prefix between alternatives

#### Java grammar

Not even slightly complete.

##### Issues

- Accepts more than one `public` or `abstract` modifier on class declaration
