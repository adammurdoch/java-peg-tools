A Java library to create parsers using Parser Expression Grammars.

Includes a sample grammar for parsing Java.

### TODO

- Use a visitor of some kind to collect token stream
- Match as much as possible on failure when speculating 
- Report where parsing stopped
- Report why parsing stopped
- Parse failures
- Fix `zeroOrMore(optional(x))`, etc
- Parse a stream
- Parse byte stream
- Construct a parse tree
