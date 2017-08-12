A Java library to create parsers using Parser Expression Grammars (PEG).

Includes a sample grammar for parsing Java.

### TODO

- Don't create a string for each match, collect locations instead
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
