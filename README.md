A Java library to create parsers using Parsing Expression Grammars, or [PEG](https://en.wikipedia.org/wiki/Parsing_expression_grammar). It provides a Java API for describing the parsing expressions and constructing a parser from these expressions.

Includes a grammar for parsing Java source.

### Parser

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
    - test: A? B where A partially matched, B no match
    - test: A? B where A partially matched and B partially matched
    - test: A* B where A partially matched zero or more times, B no match
    - test: A* B where A partially matched zero or more times, B partially matched
    - test: A+ B where A partially matched zero or more times, B no match
    - test: A+ B where A partially matched zero or more times, B partially matched
    - test: A | B where A partially matched, B no match
    - test: A | B where A partially matched and B partially matched
- Use immutable positions to represent locations in the stream
- Change contract of `BatchingMatchVisitor` so that it creates the result collector, only if required.
- Fix `zeroOrMore(optional(x))`, etc
- Report alternatives from within sequence
- Fix construction of token from non-terminal on failure
- Improve matching when there is a common prefix between sequence expressions
- Improve alternatives for mismatched negative predicate
- Test coverage for non-ascii characters

### Java grammar

Not even slightly complete.

#### Issues

- Accepts duplicate modifiers on class, interface, field declarations
- Abstract methods
- Static methods on interfaces, interface method modifiers
- Tighten up rules for field and method declarations inside class and interface declarations
