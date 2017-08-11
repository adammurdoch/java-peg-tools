A Java library to create parsers using Parser Expression Grammars.

Includes a sample grammar for parsing Java.

### TODO

- Use a visitor of some kind to collect token stream
- Fix `AnyOfExpression` and `OptionalExpression` to reset stream on failed match of candidate
- Fix `ZeroOrMoreExpression` and `OneOrMoreExpression` to reset stream on failed match of last candidate
- Throw exception on parse failure
- Parse a stream
- Parse byte stream
- Construct a parse tree
