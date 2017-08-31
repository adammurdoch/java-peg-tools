package net.rubygrapefruit.parser.peg

import net.rubygrapefruit.parser.peg.visitor.CollectingVisitor
import spock.lang.Specification
import spock.lang.Unroll

class ParserBuilderTest extends Specification {
    def builder = new ParserBuilder()

    def "can parse a string token"() {
        def expression = builder.chars("abc")

        expect:
        def parser = builder.newParser(expression)
        def result = parse(parser, "abc")
        result.tokens == ["abc"]
        result.values == [expression]
    }

    def "reports failure when input contains additional characters at end"() {
        expect:
        def parser = builder.newParser(builder.chars("abc"))
        def result = fail(parser, "abc123")
        result.tokens == ["abc"]
        result.failure == '''line 1: unexpected characters
abc123
   ^'''
    }

    @Unroll
    def "reports failure to match string token - #input"() {
        expect:
        def parser = builder.newParser(builder.chars("abc"))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input  | tokens | message
        ""     | []     | '''line 1: expected "abc"

^'''
        // TODO - should report missing char
        "ab"   | []     | '''line 1: expected "abc"
ab
^'''
        // TODO - should report missing char
        "abd"  | []     | '''line 1: expected "abc"
abd
^'''
        "ABC"  | []     | '''line 1: expected "abc"
ABC
^'''
        "123"  | []     | '''line 1: expected "abc"
123
^'''
        "1abc" | []     | '''line 1: expected "abc"
1abc
^'''
    }

    def "can parse a string token as a group"() {
        def expression = builder.chars("abc").group()

        expect:
        def parser = builder.newParser(expression)
        def result = parse(parser, "abc")
        result.tokens == ["abc"]
        result.values == [expression]
    }

    def "can parse a string character"() {
        def expression = builder.singleChar(";" as char)

        expect:
        def parser = builder.newParser(expression)
        def result = parse(parser, ";")
        result.tokens == [";"]
        result.values == [expression]
    }

    def "can parse a string character as a group"() {
        def expression = builder.singleChar(";" as char).group()

        expect:
        def parser = builder.newParser(expression)
        def result = parse(parser, ";")
        result.tokens == [";"]
        result.values == [expression]
    }

    @Unroll
    def "reports failure to match a single character - #input"() {
        expect:
        def parser = builder.newParser(builder.singleChar("x" as char))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input | tokens | message
        ""    | []     | '''line 1: expected "x"

^'''
        "X"   | []     | '''line 1: expected "x"
X
^'''
        "y"   | []     | '''line 1: expected "x"
y
^'''
        "yx"  | []     | '''line 1: expected "x"
yx
^'''
    }

    def "can parse any letter"() {
        expect:
        def parser = builder.newParser(builder.letter())
        tokens(parser, "a") == ["a"]
        tokens(parser, "Z") == ["Z"]
    }

    def "can parse any letter as group"() {
        expect:
        def parser = builder.newParser(builder.letter().group())
        tokens(parser, "a") == ["a"]
        tokens(parser, "Z") == ["Z"]
    }

    @Unroll
    def "reports failure to match a letter - #input"() {
        expect:
        def parser = builder.newParser(builder.letter())
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input | tokens | message
        ""    | []     | '''line 1: expected letter

^'''
        "1"   | []     | '''line 1: expected letter
1
^'''
        " "   | []     | '''line 1: expected letter
 
^'''
        "."   | []     | '''line 1: expected letter
.
^'''
        "1y"  | []     | '''line 1: expected letter
1y
^'''
    }

    def "can parse any character"() {
        expect:
        def parser = builder.newParser(builder.anything())
        tokens(parser, ";") == [";"]
        tokens(parser, "a") == ["a"]
    }

    def "can parse any character as group"() {
        expect:
        def parser = builder.newParser(builder.anything().group())
        tokens(parser, ";") == [";"]
        tokens(parser, "a") == ["a"]
    }

    def "reports failure parsing any character"() {
        expect:
        def parser = builder.newParser(builder.anything())
        def result = fail(parser, "")
        result.tokens == []
        result.failure == '''line 1: expected anything

^'''
    }

    def "can parse a sequence of tokens"() {
        def e1 = builder.chars("abc")
        def e2 = builder.chars("123")

        expect:
        def parser = builder.newParser(builder.sequence(e1, e2))
        def result = parse(parser, "abc123")
        result.tokens == ["abc", "123"]
        result.values == [e1, e2]
    }

    def "can parse a sequence of tokens as a group"() {
        def expression = builder.sequence(builder.chars("abc"), builder.chars("123")).group()

        expect:
        def parser = builder.newParser(expression)
        def result = parse(parser, "abc123")
        result.tokens == ["abc123"]
        result.values == [expression]
    }

    def "can parse a sequence of sequence expressions"() {
        def t1 = builder.chars("{")
        def t2 = builder.chars("}")
        def t3 = builder.chars("123")
        def t4 = builder.chars("abc")
        def e1 = builder.sequence(t1, t3, t2)
        def e2 = builder.sequence(t1, t4, t2)

        expect:
        def parser = builder.newParser(builder.sequence(e1, e2))
        def result = parse(parser, "{123}{abc}")
        result.tokens == ["{", "123", "}", "{", "abc", "}"]
        result.values == [t1, t3, t2, t1, t4, t2]
    }

    def "can parse a sequence of sequence expressions as a group"() {
        def e1 = builder.sequence(builder.chars("{"), builder.chars("123"), builder.chars("}"))
        def e2 = builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))
        def expression = builder.sequence(e1, e2).group()

        expect:
        def parser = builder.newParser(expression)
        def result = parse(parser, "{123}{abc}")
        result.tokens == ["{123}{abc}"]
        result.values == [expression]
    }

    @Unroll
    def "reports failure to match a sequence of tokens - #input"() {
        expect:
        def parser = builder.newParser(builder.sequence(builder.chars("abc"), builder.chars("123")))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input     | tokens  | message
        ""        | []      | '''line 1: expected "abc"

^'''
        "abc"     | ["abc"] | '''line 1: expected "123"
abc
   ^'''
        "abc124"  | ["abc"] | '''line 1: expected "123"
abc124
   ^'''
        "1abc123" | []      | '''line 1: expected "abc"
1abc123
^'''
        "abcx123" | ["abc"] | '''line 1: expected "123"
abcx123
   ^'''
        "abc1123" | ["abc"] | '''line 1: expected "123"
abc1123
   ^'''
    }

    @Unroll
    def "reports failure to match a sequence of sequence expressions - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(e1, e2))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input       | tokens              | message
        ""          | []                  | '''line 1: expected "abc"

^'''
        "ab"        | []                  | '''line 1: expected "abc"
ab
^'''
        "abc"       | ["abc"]             | '''line 1: expected "1"
abc
   ^'''
        "abc2"      | ["abc"]             | '''line 1: expected "1"
abc2
   ^'''
        "abc124"    | ["abc", "1"]        | '''line 1: expected "abc"
abc124
    ^'''
        "abc1abc"   | ["abc", "1", "abc"] | '''line 1: expected "2"
abc1abc
       ^'''
        "abc1abc1"  | ["abc", "1", "abc"] | '''line 1: expected "2"
abc1abc1
       ^'''
        "1abc1abc2" | []                  | '''line 1: expected "abc"
1abc1abc2
^'''
    }

    def "can parse optional token"() {
        def expression = builder.chars("abc")

        expect:
        def parser = builder.newParser(builder.optional(expression))

        def result = parse(parser, "abc")
        result.tokens == ["abc"]
        result.values == [expression]

        def result2 = parse(parser, "")
        result2.tokens == []
        result2.values == []
    }

    def "can parse optional token as a group"() {
        expect:
        def parser = builder.newParser(builder.optional(builder.chars("abc")).group())
        tokens(parser, "abc") == ["abc"]
        tokens(parser, "") == []
    }

    def "can parse optional sequence expression"() {
        expect:
        def parser = builder.newParser(builder.optional(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))))
        tokens(parser, "{abc}") == ["{", "abc", "}"]
        tokens(parser, "") == []
    }

    @Unroll
    def "reports failure to match optional sequence expression - #input"() {
        expect:
        def parser = builder.newParser(builder.optional(builder.sequence(builder.chars("abc"), builder.chars("1"))))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input   | tokens       | message
        "abc"   | ["abc"]      | '''line 1: expected "1"
abc
   ^'''
        "abcx"  | ["abc"]      | '''line 1: expected "1"
abcx
   ^'''
        "abc2"  | ["abc"]      | '''line 1: expected "1"
abc2
   ^'''
        "abc1z" | ["abc", "1"] | '''line 1: unexpected characters
abc1z
    ^'''
        // TODO - should partially match
        "ab"    | []           | '''line 1: unexpected characters
ab
^'''
        "1"     | []           | '''line 1: unexpected characters
1
^'''
        "x"     | []           | '''line 1: unexpected characters
x
^'''
        "xabc"  | []           | '''line 1: unexpected characters
xabc
^'''
    }

    def "can parse optional sequence expression as a group"() {
        expect:
        def parser = builder.newParser(builder.optional(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))).group())
        tokens(parser, "{abc}") == ["{abc}"]
        tokens(parser, "") == []
    }

    @Unroll
    def "reports failure to match sequence with optional expression - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("1"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.optional(e1), e2))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input     | tokens            | message
        ""        | []                | '''line 1: expected "1" or "abc"

^'''
        "abd"     | []                | '''line 1: expected "1" or "abc"
abd
^'''
        "abc"     | ["abc"]           | '''line 1: expected "1"
abc
   ^'''
        "abc2"    | ["abc"]           | '''line 1: expected "1"
abc2
   ^'''
        "1"       | ["1"]             | '''line 1: expected "2"
1
 ^'''
        "13"      | ["1"]             | '''line 1: expected "2"
13
 ^'''
        "abc11"   | ["abc", "1", "1"] | '''line 1: expected "2"
abc11
     ^'''
        "abc113"  | ["abc", "1", "1"] | '''line 1: expected "2"
abc113
     ^'''
        "1abc112" | ["1"]             | '''line 1: expected "2"
1abc112
 ^'''
    }

    @Unroll
    def "reports failure to match sequence with optional expressions - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("1"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.optional(e1), builder.optional(e2), builder.chars(";")))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input     | tokens                 | message
        ""        | []                     | '''line 1: expected "1", ";" or "abc"

^'''
        "adc"     | []                     | '''line 1: expected "1", ";" or "abc"
adc
^'''
        "abc"     | ["abc"]                | '''line 1: expected "1"
abc
   ^'''
        "abc2"    | ["abc"]                | '''line 1: expected "1"
abc2
   ^'''
        "abc1"    | ["abc", "1"]           | '''line 1: expected "1" or ";"
abc1
    ^'''
        "abc1x"   | ["abc", "1"]           | '''line 1: expected "1" or ";"
abc1x
    ^'''
        "abc11"   | ["abc", "1", "1"]      | '''line 1: expected "2"
abc11
     ^'''
        "abc11x"  | ["abc", "1", "1"]      | '''line 1: expected "2"
abc11x
     ^'''
        "abc112"  | ["abc", "1", "1", "2"] | '''line 1: expected ";"
abc112
      ^'''
        "abc112x" | ["abc", "1", "1", "2"] | '''line 1: expected ";"
abc112x
      ^'''
        "1;"      | ["1"]                  | '''line 1: expected "2"
1;
 ^'''
        "1x"      | ["1"]                  | '''line 1: expected "2"
1x
 ^'''
        "12"      | ["1", "2"]             | '''line 1: expected ";"
12
  ^'''
        "x"       | []                     | '''line 1: expected "1", ";" or "abc"
x
^'''
    }

    @Unroll
    def "reports failure to match optional expression with common prefix with following expression - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.optional(e1), e2))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input        | tokens                   | message
        ""           | []                       | '''line 1: expected "abc"

^'''
        "abc"        | ["abc"]                  | '''line 1: expected "1" or "2"
abc
   ^'''
        "abc1"       | ["abc", "1"]             | '''line 1: expected "2"
abc1
    ^'''
        "abc1x"      | ["abc", "1"]             | '''line 1: expected "2"
abc1x
    ^'''
        "abc1xabc2"  | ["abc", "1"]             | '''line 1: expected "2"
abc1xabc2
    ^'''
        "abc3"       | ["abc"]                  | '''line 1: expected "1" or "2"
abc3
   ^'''
        "abc12abc"   | ["abc", "1", "2", "abc"] | '''line 1: expected "2"
abc12abc
        ^'''
        "abc12abc3"  | ["abc", "1", "2", "abc"] | '''line 1: expected "2"
abc12abc3
        ^'''
        "abc12xabc2" | ["abc", "1", "2"]        | '''line 1: expected "abc"
abc12xabc2
     ^'''
    }

    def "can parse zero or more tokens"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.chars("abc")))
        tokens(parser, "abcabc") == ["abc", "abc"]
        tokens(parser, "abc") == ["abc"]
        tokens(parser, "") == []
    }

    def "can parse zero or more tokens as a group"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.chars("abc")).group())
        tokens(parser, "abcabc") == ["abcabc"]
        tokens(parser, "abc") == ["abc"]
        tokens(parser, "") == []
    }

    def "can parse zero or more sequence expressions"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))))
        tokens(parser, "{abc}{abc}") == ["{", "abc", "}", "{", "abc", "}"]
        tokens(parser, "{abc}") == ["{", "abc", "}"]
        tokens(parser, "") == []
    }

    def "can parse zero or more sequence expressions as a group"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))).group())
        tokens(parser, "{abc}{abc}") == ["{abc}{abc}"]
        tokens(parser, "{abc}") == ["{abc}"]
        tokens(parser, "") == []
    }

    @Unroll
    def "reports failure to match zero or more sequence expressions as a group - #input"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))).group())
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input         | tokens         | message
        "{"           | ["{"]          | '''line 1: expected "abc"
{
 ^'''
        "{x"          | ["{"]          | '''line 1: expected "abc"
{x
 ^'''
        "{abc"        | ["{abc"]       | '''line 1: expected "}"
{abc
    ^'''
        "{abcx"       | ["{abc"]       | '''line 1: expected "}"
{abcx
    ^'''
        // TODO - missing an alternative
        "{abc}x"      | ["{abc}"]      | '''line 1: unexpected characters
{abc}x
     ^'''
        "{abc}{"      | ["{abc}{"]     | '''line 1: expected "abc"
{abc}{
      ^'''
        "{abc}{abc"   | ["{abc}{abc"]  | '''line 1: expected "}"
{abc}{abc
         ^'''
        "{abc}{x"     | ["{abc}{"]     | '''line 1: expected "abc"
{abc}{x
      ^'''
        "{abc}{abcx"  | ["{abc}{abc"]  | '''line 1: expected "}"
{abc}{abcx
         ^'''
        // TODO - missing an alternative
        "{abc}{abc}x" | ["{abc}{abc}"] | '''line 1: unexpected characters
{abc}{abc}x
          ^'''
    }

    def "can parse zero or more expressions with common prefix with following expression"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.zeroOrMore(e1), e2))
        tokens(parser, "abc1abc1abc2") == ["abc", "1", "abc", "1", "abc", "2"]
        tokens(parser, "abc1abc2") == ["abc", "1", "abc", "2"]
        tokens(parser, "abc2") == ["abc", "2"]
    }

    @Unroll
    def "reports failure to match zero or more expression with common prefix with following expression - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.zeroOrMore(e1), e2))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input           | tokens                                    | message
        ""              | []                                        | '''line 1: expected "abc"

^'''
        "abc"           | ["abc"]                                   | '''line 1: expected "1" or "2"
abc
   ^'''
        "abc1"          | ["abc", "1"]                              | '''line 1: expected "2"
abc1
    ^'''
        "abc1x"         | ["abc", "1"]                              | '''line 1: expected "2"
abc1x
    ^'''
        "abc1xabc2"     | ["abc", "1"]                              | '''line 1: expected "2"
abc1xabc2
    ^'''
        "abc3"          | ["abc"]                                   | '''line 1: expected "1" or "2"
abc3
   ^'''
        "abc12abc"      | ["abc", "1", "2", "abc"]                  | '''line 1: expected "1" or "2"
abc12abc
        ^'''
        "abc12abc3"     | ["abc", "1", "2", "abc"]                  | '''line 1: expected "1" or "2"
abc12abc3
        ^'''
        "abc12xabc2"    | ["abc", "1", "2"]                         | '''line 1: expected "abc"
abc12xabc2
     ^'''
        "abc12abc1"     | ["abc", "1", "2", "abc", "1"]             | '''line 1: expected "2"
abc12abc1
         ^'''
        "abc12abc1x"    | ["abc", "1", "2", "abc", "1"]             | '''line 1: expected "2"
abc12abc1x
         ^'''
        "abc12abc12"    | ["abc", "1", "2", "abc", "1", "2"]        | '''line 1: expected "abc"
abc12abc12
          ^'''
        "abc12abc12abc" | ["abc", "1", "2", "abc", "1", "2", "abc"] | '''line 1: expected "1" or "2"
abc12abc12abc
             ^'''
    }

    @Unroll
    def "reports failure to match zero or more expression as group with common prefix with following expression - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.zeroOrMore(e1).group(), e2))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input           | tokens                | message
        ""              | []                    | '''line 1: expected "abc"

^'''
        "abc"           | ["abc"]               | '''line 1: expected "1" or "2"
abc
   ^'''
        "abc1"          | ["abc1"]              | '''line 1: expected "2"
abc1
    ^'''
        "abc1x"         | ["abc1"]              | '''line 1: expected "2"
abc1x
    ^'''
        "abc1xabc2"     | ["abc1"]              | '''line 1: expected "2"
abc1xabc2
    ^'''
        "abc3"          | ["abc"]               | '''line 1: expected "1" or "2"
abc3
   ^'''
        "abc12abc"      | ["abc12", "abc"]      | '''line 1: expected "1" or "2"
abc12abc
        ^'''
        "abc12abc3"     | ["abc12", "abc"]      | '''line 1: expected "1" or "2"
abc12abc3
        ^'''
        "abc12xabc2"    | ["abc12"]             | '''line 1: expected "abc"
abc12xabc2
     ^'''
        "abc12abc1"     | ["abc12", "abc1"]     | '''line 1: expected "2"
abc12abc1
         ^'''
        "abc12abc1x"    | ["abc12", "abc1"]     | '''line 1: expected "2"
abc12abc1x
         ^'''
        "abc12abc12"    | ["abc12abc12"]        | '''line 1: expected "abc"
abc12abc12
          ^'''
        "abc12abc12abc" | ["abc12abc12", "abc"] | '''line 1: expected "1" or "2"
abc12abc12abc
             ^'''
    }

    def "can parse one or more tokens"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.chars("abc")))
        tokens(parser, "abcabc") == ["abc", "abc"]
        tokens(parser, "abc") == ["abc"]
    }

    def "can parse one or more tokens as a group"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.chars("abc")).group())
        tokens(parser, "abcabc") == ["abcabc"]
        tokens(parser, "abc") == ["abc"]
    }

    def "can parse one or more sequence expressions"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))))
        tokens(parser, "{abc}{abc}") == ["{", "abc", "}", "{", "abc", "}"]
        tokens(parser, "{abc}") == ["{", "abc", "}"]
    }

    @Unroll
    def "reports failure to match one or more sequence expressions - #input"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input         | tokens                             | message
        ""            | []                                 | '''line 1: expected "{"

^'''
        "{"           | ["{"]                              | '''line 1: expected "abc"
{
 ^'''
        "{x"          | ["{"]                              | '''line 1: expected "abc"
{x
 ^'''
        "{abc"        | ["{", "abc"]                       | '''line 1: expected "}"
{abc
    ^'''
        "{abc;"       | ["{", "abc"]                       | '''line 1: expected "}"
{abc;
    ^'''
        // TODO - missing alternative '{'
        "{abc}x"      | ["{", "abc", "}"]                  | '''line 1: unexpected characters
{abc}x
     ^'''
        "{abc}{"      | ["{", "abc", "}", "{"]             | '''line 1: expected "abc"
{abc}{
      ^'''
        "{abc}{abc;"  | ["{", "abc", "}", "{", "abc"]      | '''line 1: expected "}"
{abc}{abc;
         ^'''
        // TODO - missing alternative '{'
        "{abc}{abc}x" | ["{", "abc", "}", "{", "abc", "}"] | '''line 1: unexpected characters
{abc}{abc}x
          ^'''
        "x"           | []                                 | '''line 1: expected "{"
x
^'''
    }

    def "can parse one or more sequence expressions as a group"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))).group())
        tokens(parser, "{abc}{abc}") == ["{abc}{abc}"]
        tokens(parser, "{abc}") == ["{abc}"]
    }

    def "can parse one or more expressions with common prefix with following expression"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.oneOrMore(e1), e2))
        tokens(parser, "abc1abc1abc2") == ["abc", "1", "abc", "1", "abc", "2"]
        tokens(parser, "abc1abc2") == ["abc", "1", "abc", "2"]
    }

    def "can parse one of several alternative tokens"() {
        expect:
        def parser = builder.newParser(builder.oneOf(builder.chars("abc"), builder.chars("123")))
        tokens(parser, "123") == ["123"]
        tokens(parser, "abc") == ["abc"]
    }

    def "can parse one of several alternative tokens as a group"() {
        expect:
        def parser = builder.newParser(builder.oneOf(builder.chars("abc"), builder.chars("123")).group())
        tokens(parser, "123") == ["123"]
        tokens(parser, "abc") == ["abc"]
    }

    def "can parse one of several alternative sequence expressions"() {
        def e1 = builder.sequence(builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("2"), builder.chars("1"))

        expect:
        def parser = builder.newParser(builder.oneOf(e1, e2))
        tokens(parser, "12") == ["1", "2"]
        tokens(parser, "21") == ["2", "1"]
    }

    def "can parse one of several alternative sequence expressions as a group"() {
        def e1 = builder.sequence(builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("2"), builder.chars("1"))

        expect:
        def parser = builder.newParser(builder.oneOf(e1, e2).group())
        tokens(parser, "12") == ["12"]
        tokens(parser, "21") == ["21"]
    }

    def "can parse one of several alternative tokens with common prefix"() {
        expect:
        def parser = builder.newParser(builder.oneOf(builder.chars("abc1"), builder.chars("abc2"), builder.chars("abc")))
        tokens(parser, "abc") == ["abc"]
        tokens(parser, "abc1") == ["abc1"]
        tokens(parser, "abc2") == ["abc2"]
    }

    def "can parse one of several alternative expressions with common prefix"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.oneOf(e1, e2))
        tokens(parser, "abc1") == ["abc", "1"]
        tokens(parser, "abc2") == ["abc", "2"]
    }

    @Unroll
    def "reports failure to match one of several alternative expressions - #input"() {
        def e1 = builder.sequence(builder.chars("ab"), builder.chars("12"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("1"))

        expect:
        def parser = builder.newParser(builder.oneOf(e1, e2))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input   | tokens       | message
        ""      | []           | '''line 1: expected "ab" or "abc"

^'''
        "a"     | []           | '''line 1: expected "ab" or "abc"
a
^'''
        "ab"    | ["ab"]       | '''line 1: expected "12"
ab
  ^'''
        "ab1"   | ["ab"]       | '''line 1: expected "12"
ab1
  ^'''
        "ab13"  | ["ab"]       | '''line 1: expected "12"
ab13
  ^'''
        "ab12x" | ["ab", "12"] | '''line 1: unexpected characters
ab12x
    ^'''
        "abd"   | ["ab"]       | '''line 1: expected "12"
abd
  ^'''
        "abc"   | ["abc"]      | '''line 1: expected "1"
abc
   ^'''
        "abc2"  | ["abc"]      | '''line 1: expected "1"
abc2
   ^'''
        "abc1x" | ["abc", "1"] | '''line 1: unexpected characters
abc1x
    ^'''
        "adc"   | []           | '''line 1: expected "ab" or "abc"
adc
^'''
    }

    @Unroll
    def "reports failure to match one of several alternative expressions with common prefix and suffix - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.oneOf(e1, e2))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input   | tokens       | message
        ""      | []           | '''line 1: expected "abc"

^'''
        "ab"    | []           | '''line 1: expected "abc"
ab
^'''
        "abc"   | ["abc"]      | '''line 1: expected "1" or "2"
abc
   ^'''
        "abc3"  | ["abc"]      | '''line 1: expected "1" or "2"
abc3
   ^'''
        "abc1"  | ["abc", "1"] | '''line 1: expected "2"
abc1
    ^'''
        "abc1x" | ["abc", "1"] | '''line 1: expected "2"
abc1x
    ^'''
        "abc2"  | ["abc", "2"] | '''line 1: expected "2"
abc2
    ^'''
        "abc2x" | ["abc", "2"] | '''line 1: expected "2"
abc2x
    ^'''
    }

    def "can parse one of several alternative expressions with common prefix with following expression"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))
        def e3 = builder.sequence(builder.chars("abc"), builder.chars("3"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.oneOf(e1, e2), e3))
        tokens(parser, "abc1abc3") == ["abc", "1", "abc", "3"]
        tokens(parser, "abc2abc3") == ["abc", "2", "abc", "3"]
    }

    def "can parse a sequence of optional tokens"() {
        def e1 = builder.optional(builder.chars("abc"))
        def e2 = builder.optional(builder.chars("123"))

        expect:
        def parser = builder.newParser(builder.sequence(e1, e2))
        tokens(parser, "abc123") == ["abc", "123"]
        tokens(parser, "abc") == ["abc"]
        tokens(parser, "123") == ["123"]
        tokens(parser, "") == []
    }

    def "can parse a sequence of optional expressions with common prefix"() {
        def e1 = builder.optional(builder.sequence(builder.chars("abc"), builder.chars("1")))
        def e2 = builder.optional(builder.sequence(builder.chars("abc"), builder.chars("2")))

        expect:
        def parser = builder.newParser(builder.sequence(e1, e2))
        tokens(parser, "abc1abc2") == ["abc", "1", "abc", "2"]
        tokens(parser, "abc1") == ["abc", "1"]
        tokens(parser, "abc2") == ["abc", "2"]
        tokens(parser, "") == []
    }

    def "can parse optional expression with common prefix with following expression"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.optional(e1), e2))
        tokens(parser, "abc1abc2") == ["abc", "1", "abc", "2"]
        tokens(parser, "abc2") == ["abc", "2"]
    }

    def "can parse negative predicate"() {
        def e1 = builder.chars("abc")
        def e2 = builder.zeroOrMore(builder.letter()).group()

        expect:
        def parser = builder.newParser(builder.sequence(builder.not(e1), e2))
        tokens(parser, "") == []
        tokens(parser, "ab") == ["ab"]
        tokens(parser, "cdef") == ["cdef"]
        tokens(parser, "cabc") == ["cabc"]
        tokens(parser, "abd") == ["abd"]
    }

    @Unroll
    def "reports failure to match negative predicate - #input"() {
        def e1 = builder.chars("abc")
        def e2 = builder.zeroOrMore(builder.letter()).group()

        expect:
        def parser = builder.newParser(builder.sequence(builder.not(e1), e2))
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input    | tokens | message
        // TODO - missing alternatives
        "abc"    | []     | '''line 1:
abc
^'''
        // TODO - missing alternatives
        "abca"   | []     | '''line 1:
abca
^'''
        // TODO - missing alternatives
        "abc123" | []     | '''line 1:
abc123
^'''
        // TODO - missing alternatives
        "123"    | []     | '''line 1: unexpected characters
123
^'''
    }

    def List<String> tokens(Parser parser, String str) {
        def visitor = parser.parse(str, new CollectingVisitor())
        assert visitor.failure == null
        return visitor.tokens
    }

    def CollectingVisitor parse(Parser parser, String str) {
        def visitor = parser.parse(str, new CollectingVisitor())
        assert visitor.failure == null
        return visitor
    }

    def CollectingVisitor fail(Parser parser, String str) {
        def visitor = parser.parse(str, new CollectingVisitor())
        assert visitor.failure != null
        return visitor
    }
}
