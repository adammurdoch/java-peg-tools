# Java PEG Utils

A Java library to create parsers using Parsing Expression Grammars, or [PEG](https://en.wikipedia.org/wiki/Parsing_expression_grammar). It provides a Java API for describing the parsing expressions and constructing a parser from these expressions.

The library requires Java 7 or later.

Includes a grammar for parsing Java source.

## Parser

The implementation is in the very early stages and can scan the input into tokens, but does not yet provide a parse tree as output.

### Features

- Java API to construct a parser. No need to define the parser in some other language to generate the parser.
- Thread-safe parsers can be constructed once and used by multiple threads concurrently.
- Good quality error messages.

#### Expressions

Terminals:

- `chars(String)` matches the given string.
- `singleChar(ch)` matches the given character.
- `letter(h)` matches a single letter character.
- `anything()` matches a single character.

Non-terminals:

- `sequence(expressions)` matches the given expressions in order.
- `oneOf(expressions)` matches any one of the given expressions. Matches the expressions in order, stopping at the first match.
- `optional(expression)` matches zero or one occurrences of the given expression. 
- `zeroOrMore(expression)` matches zero or more occurrences of the given expression. Matching is greedy.
- `oneOrMore(expression)` matches one or more occurrences of the given expression. Matching is greedy.

Look ahead:

- `not(expression)` matches anything that does not match the given expression, but does not consume any input

Other:

- `reference()` creates an expression that is a placeholder for some other expression. Can be used to create recursive expressions.
- `backReference()` creates an expression that matches the text matched by another expression. Can be used to create contextual grammars.

### Missing features

- Construct a parse tree
- Construct an AST or arbitrary result
- Parse a stream
- Parse byte stream
- Basic error recovery
- Push parsing as well as pull
- Expose information about match location
- Recursive expressions
- Back references
- Expression that accepts char in range
- Expression that takes a char predicate
- Expressions that match characters case insensitive
- Expression that takes a predicate to accept or reject the result of another expression (with some kind of failure message).
- Expression that transforms the result of another expression
- Expression that transforms the result of an expression into another expression to use for the next match

### Fixes

- Thread-safe back reference implementation
- More efficient back reference implementation, reuse error handling
- Match as much of the result as possible on failure when speculating (optional, one-or-more, zero-or-more) 
    - test: A? B where A partially matched, B no match
    - test: A? B where A partially matched and B partially matched
    - test: A* B where A partially matched zero or more times, B no match
    - test: A* B where A partially matched zero or more times, B partially matched
    - test: A+ B where A partially matched zero or more times, B no match
    - test: A+ B where A partially matched zero or more times, B partially matched
    - test: A | B where A partially matched, B no match
    - test: A | B where A partially matched and B partially matched
- Change contract of `BatchingMatchVisitor` so that it creates the result collector, only if required.
- Fix `zeroOrMore(optional(x))`, etc
- Report alternatives from within sequence
- Fix construction of token from non-terminal on failure
- Improve matching when there is a common prefix between sequence expressions
- Improve alternatives for mismatched negative predicate
- Test coverage for non-ascii characters
- Improve error message when parsing stops at end of input, or on an empty line (or both)
- Improve error message on mismatched negative lookahead
- On failure due to unexpected end-of-input rewind back past ignored expressions to report issue
- On failure to match last element of sequence, report where the sequence started if not on same line
- Flatten expressions that act on chars and that are grouped for faster matching

## Java grammar

Nominally supports Java 8. Is not even slightly complete.

### Issues

- A keyword should not be treated as an identifier
- Highlighting broken for class constructor
- Duplicate modifiers on class, interface, field, constructor, method declarations.
- Abstract methods
- Static methods on interfaces, interface method modifiers
- Tighten up rules for field and method declarations inside class and interface declarations
- Shouldn't require a space between type param and `extends` or `implements` keyword

## References

- [Java 8 syntax](https://docs.oracle.com/javase/specs/jls/se8/html/jls-19.html)
- [CommonMark spec](http://spec.commonmark.org)
