package net.rubygrapefruit.parser.peg

import net.rubygrapefruit.parser.peg.visitor.CollectingVisitor
import spock.lang.Specification
import spock.lang.Unroll

class ParserBuilderTest extends Specification {
    def builder = new ParserBuilder()

    def "can parse a string token"() {
        expect:
        def parser = builder.newParser(builder.chars("abc"))
        parse(parser, "abc") == ["abc"]
    }

    def "reports failure when input contains additional characters at end"() {
        expect:
        def parser = builder.newParser(builder.chars("abc"))
        def result = fail(parser, "abc123")
        result.result == ["abc"]
        result.failure == "extra input at offset 3: [123]"
    }

    @Unroll
    def "reports failure to match string token - #input"() {
        expect:
        def parser = builder.newParser(builder.chars("abc"))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input  | tokens | message
        ""     | []     | 'stopped at offset 0: end of input\nexpected: "abc"'
        // TODO - should report missing char
        "ab"   | []     | 'stopped at offset 0: [ab]\nexpected: "abc"'
        // TODO - should report missing char
        "abd"  | []     | 'stopped at offset 0: [abd]\nexpected: "abc"'
        "ABC"  | []     | 'stopped at offset 0: [ABC]\nexpected: "abc"'
        "123"  | []     | 'stopped at offset 0: [123]\nexpected: "abc"'
        "1abc" | []     | 'stopped at offset 0: [1abc]\nexpected: "abc"'
    }

    def "can parse a string token as a group"() {
        expect:
        def parser = builder.newParser(builder.chars("abc").group())
        parse(parser, "abc") == ["abc"]
    }

    def "can parse a string character"() {
        expect:
        def parser = builder.newParser(builder.singleChar(";" as char))
        parse(parser, ";") == [";"]
    }

    @Unroll
    def "reports failure to match a string character - #input"() {
        expect:
        def parser = builder.newParser(builder.singleChar("x" as char))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input | tokens | message
        ""    | []     | 'stopped at offset 0: end of input\nexpected: "x"'
        "X"   | []     | 'stopped at offset 0: [X]\nexpected: "x"'
        "y"   | []     | 'stopped at offset 0: [y]\nexpected: "x"'
        "yx"  | []     | 'stopped at offset 0: [yx]\nexpected: "x"'
    }

    def "can parse any character"() {
        expect:
        def parser = builder.newParser(builder.anything())
        parse(parser, ";") == [";"]
        parse(parser, "a") == ["a"]
    }

    def "can parse any character as group"() {
        expect:
        def parser = builder.newParser(builder.anything().group())
        parse(parser, ";") == [";"]
        parse(parser, "a") == ["a"]
    }

    def "reports failure parsing any character"() {
        expect:
        def parser = builder.newParser(builder.anything())
        def result = fail(parser, "")
        result.result == []
        result.failure == "stopped at offset 0: end of input\nexpected: anything"
    }

    def "can parse a string character as a group"() {
        expect:
        def parser = builder.newParser(builder.singleChar(";" as char).group())
        parse(parser, ";") == [";"]
    }

    def "can parse a sequence of tokens"() {
        expect:
        def parser = builder.newParser(builder.sequence(builder.chars("abc"), builder.chars("123")))
        parse(parser, "abc123") == ["abc", "123"]
    }

    def "can parse a sequence of tokens as a group"() {
        expect:
        def parser = builder.newParser(builder.sequence(builder.chars("abc"), builder.chars("123")).group())
        parse(parser, "abc123") == ["abc123"]
    }

    def "can parse a sequence of sequence expressions"() {
        def e1 = builder.sequence(builder.chars("{"), builder.chars("123"), builder.chars("}"))
        def e2 = builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))

        expect:
        def parser = builder.newParser(builder.sequence(e1, e2))
        parse(parser, "{123}{abc}") == ["{", "123", "}", "{", "abc", "}"]
    }

    def "can parse a sequence of sequence expressions as a group"() {
        def e1 = builder.sequence(builder.chars("{"), builder.chars("123"), builder.chars("}"))
        def e2 = builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))

        expect:
        def parser = builder.newParser(builder.sequence(e1, e2).group())
        parse(parser, "{123}{abc}") == ["{123}{abc}"]
    }

    @Unroll
    def "reports failure to match a sequence of tokens - #input"() {
        expect:
        def parser = builder.newParser(builder.sequence(builder.chars("abc"), builder.chars("123")))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input     | tokens  | message
        ""        | []      | 'stopped at offset 0: end of input\nexpected: "abc"'
        "abc"     | ["abc"] | 'stopped at offset 3: end of input\nexpected: "123"'
        "abc124"  | ["abc"] | 'stopped at offset 3: [124]\nexpected: "123"'
        "1abc123" | []      | 'stopped at offset 0: [1abc123]\nexpected: "abc"'
        "abcx123" | ["abc"] | 'stopped at offset 3: [x123]\nexpected: "123"'
        "abc1123" | ["abc"] | 'stopped at offset 3: [1123]\nexpected: "123"'
    }

    @Unroll
    def "reports failure to match a sequence of sequence expressions - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(e1, e2))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input       | tokens              | message
        ""          | []                  | 'stopped at offset 0: end of input\nexpected: "abc"'
        "ab"        | []                  | 'stopped at offset 0: [ab]\nexpected: "abc"'
        "abc"       | ["abc"]             | 'stopped at offset 3: end of input\nexpected: "1"'
        "abc2"      | ["abc"]             | 'stopped at offset 3: [2]\nexpected: "1"'
        "abc124"    | ["abc", "1"]        | 'stopped at offset 4: [24]\nexpected: "abc"'
        "abc1abc"   | ["abc", "1", "abc"] | 'stopped at offset 7: end of input\nexpected: "2"'
        "abc1abc1"  | ["abc", "1", "abc"] | 'stopped at offset 7: [1]\nexpected: "2"'
        "1abc1abc2" | []                  | 'stopped at offset 0: [1abc1abc2]\nexpected: "abc"'
    }

    def "can parse optional token"() {
        expect:
        def parser = builder.newParser(builder.optional(builder.chars("abc")))
        parse(parser, "abc") == ["abc"]
        parse(parser, "") == []
    }

    def "can parse optional token as a group"() {
        expect:
        def parser = builder.newParser(builder.optional(builder.chars("abc")).group())
        parse(parser, "abc") == ["abc"]
        parse(parser, "") == []
    }

    def "can parse optional sequence expression"() {
        expect:
        def parser = builder.newParser(builder.optional(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))))
        parse(parser, "{abc}") == ["{", "abc", "}"]
        parse(parser, "") == []
    }

    @Unroll
    def "reports failure to match optional sequence expression - #input"() {
        expect:
        def parser = builder.newParser(builder.optional(builder.sequence(builder.chars("abc"), builder.chars("1"))))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input   | tokens       | message
        "abc"   | ["abc"]      | 'stopped at offset 3: end of input\nexpected: "1"'
        "abcx"  | ["abc"]      | 'stopped at offset 3: [x]\nexpected: "1"'
        "abc2"  | ["abc"]      | 'stopped at offset 3: [2]\nexpected: "1"'
        "abc1z" | ["abc", "1"] | 'extra input at offset 4: [z]'
        "ab"    | []           | 'extra input at offset 0: [ab]'
        "1"     | []           | 'extra input at offset 0: [1]'
        "x"     | []           | 'extra input at offset 0: [x]'
        "xabc"  | []           | 'extra input at offset 0: [xabc]'
    }

    def "can parse optional sequence expression as a group"() {
        expect:
        def parser = builder.newParser(builder.optional(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))).group())
        parse(parser, "{abc}") == ["{abc}"]
        parse(parser, "") == []
    }

    @Unroll
    def "reports failure to match sequence with optional expression - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("1"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.optional(e1), e2))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input     | tokens            | message
        ""        | []                | 'stopped at offset 0: end of input\nexpected: "1", "abc"'
        "abd"     | []                | 'stopped at offset 0: [abd]\nexpected: "1", "abc"'
        "abc"     | ["abc"]           | 'stopped at offset 3: end of input\nexpected: "1"'
        "abc2"    | ["abc"]           | 'stopped at offset 3: [2]\nexpected: "1"'
        "1"       | ["1"]             | 'stopped at offset 1: end of input\nexpected: "2"'
        "13"      | ["1"]             | 'stopped at offset 1: [3]\nexpected: "2"'
        "abc11"   | ["abc", "1", "1"] | 'stopped at offset 5: end of input\nexpected: "2"'
        "abc113"  | ["abc", "1", "1"] | 'stopped at offset 5: [3]\nexpected: "2"'
        "1abc112" | ["1"]             | 'stopped at offset 1: [abc112]\nexpected: "2"'
    }

    @Unroll
    def "reports failure to match sequence with optional expressions - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("1"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.optional(e1), builder.optional(e2), builder.chars(";")))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input     | tokens                 | message
        ""        | []                     | 'stopped at offset 0: end of input\nexpected: "1", ";", "abc"'
        "adc"     | []                     | 'stopped at offset 0: [adc]\nexpected: "1", ";", "abc"'
        "abc"     | ["abc"]                | 'stopped at offset 3: end of input\nexpected: "1"'
        "abc2"    | ["abc"]                | 'stopped at offset 3: [2]\nexpected: "1"'
        "abc1"    | ["abc", "1"]           | 'stopped at offset 4: end of input\nexpected: "1", ";"'
        "abc1x"   | ["abc", "1"]           | 'stopped at offset 4: [x]\nexpected: "1", ";"'
        "abc11"   | ["abc", "1", "1"]      | 'stopped at offset 5: end of input\nexpected: "2"'
        "abc11x"  | ["abc", "1", "1"]      | 'stopped at offset 5: [x]\nexpected: "2"'
        "abc112"  | ["abc", "1", "1", "2"] | 'stopped at offset 6: end of input\nexpected: ";"'
        "abc112x" | ["abc", "1", "1", "2"] | 'stopped at offset 6: [x]\nexpected: ";"'
        "1;"      | ["1"]                  | 'stopped at offset 1: [;]\nexpected: "2"'
        "1x"      | ["1"]                  | 'stopped at offset 1: [x]\nexpected: "2"'
        "12"      | ["1", "2"]             | 'stopped at offset 2: end of input\nexpected: ";"'
        "x"       | []                     | 'stopped at offset 0: [x]\nexpected: "1", ";", "abc"'
    }

    @Unroll
    def "reports failure to match optional expression with common prefix with following expression - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.optional(e1), e2))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input        | tokens                   | message
        ""           | []                       | 'stopped at offset 0: end of input\nexpected: "abc"'
        "abc"        | ["abc"]                  | 'stopped at offset 3: end of input\nexpected: "1", "2"'
        "abc1"       | ["abc", "1"]             | 'stopped at offset 4: end of input\nexpected: "2"'
        "abc1x"      | ["abc", "1"]             | 'stopped at offset 4: [x]\nexpected: "2"'
        "abc1xabc2"  | ["abc", "1"]             | 'stopped at offset 4: [xabc2]\nexpected: "2"'
        "abc3"       | ["abc"]                  | 'stopped at offset 3: [3]\nexpected: "1", "2"'
        "abc12abc"   | ["abc", "1", "2", "abc"] | 'stopped at offset 8: end of input\nexpected: "2"'
        "abc12abc3"  | ["abc", "1", "2", "abc"] | 'stopped at offset 8: [3]\nexpected: "2"'
        "abc12xabc2" | ["abc", "1", "2"]        | 'stopped at offset 5: [xabc2]\nexpected: "abc"'
    }

    def "can parse zero or more tokens"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.chars("abc")))
        parse(parser, "abcabc") == ["abc", "abc"]
        parse(parser, "abc") == ["abc"]
        parse(parser, "") == []
    }

    def "can parse zero or more tokens as a group"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.chars("abc")).group())
        parse(parser, "abcabc") == ["abcabc"]
        parse(parser, "abc") == ["abc"]
        parse(parser, "") == []
    }

    def "can parse zero or more sequence expressions"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))))
        parse(parser, "{abc}{abc}") == ["{", "abc", "}", "{", "abc", "}"]
        parse(parser, "{abc}") == ["{", "abc", "}"]
        parse(parser, "") == []
    }

    def "can parse zero or more sequence expressions as a group"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))).group())
        parse(parser, "{abc}{abc}") == ["{abc}{abc}"]
        parse(parser, "{abc}") == ["{abc}"]
        parse(parser, "") == []
    }

    @Unroll
    def "reports failure to match zero or more sequence expressions as a group - #input"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))).group())
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input         | tokens         | message
        "{"           | ["{"]          | 'stopped at offset 1: end of input\nexpected: "abc"'
        "{x"          | ["{"]          | 'stopped at offset 1: [x]\nexpected: "abc"'
        "{abc"        | ["{abc"]       | 'stopped at offset 4: end of input\nexpected: "}"'
        "{abcx"       | ["{abc"]       | 'stopped at offset 4: [x]\nexpected: "}"'
        // TODO - missing an alternative
        "{abc}x"      | ["{abc}"]      | 'extra input at offset 5: [x]'
        "{abc}{"      | ["{abc}{"]     | 'stopped at offset 6: end of input\nexpected: "abc"'
        "{abc}{abc"   | ["{abc}{abc"]  | 'stopped at offset 9: end of input\nexpected: "}"'
        "{abc}{x"     | ["{abc}{"]     | 'stopped at offset 6: [x]\nexpected: "abc"'
        "{abc}{abcx"  | ["{abc}{abc"]  | 'stopped at offset 9: [x]\nexpected: "}"'
        // TODO - missing an alternative
        "{abc}{abc}x" | ["{abc}{abc}"] | 'extra input at offset 10: [x]'
    }

    def "can parse zero or more expressions with common prefix with following expression"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.zeroOrMore(e1), e2))
        parse(parser, "abc1abc1abc2") == ["abc", "1", "abc", "1", "abc", "2"]
        parse(parser, "abc1abc2") == ["abc", "1", "abc", "2"]
        parse(parser, "abc2") == ["abc", "2"]
    }

    @Unroll
    def "reports failure to match zero or more expression with common prefix with following expression - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.zeroOrMore(e1), e2))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input           | tokens                                    | message
        ""              | []                                        | 'stopped at offset 0: end of input\nexpected: "abc"'
        "abc"           | ["abc"]                                   | 'stopped at offset 3: end of input\nexpected: "1", "2"'
        "abc1"          | ["abc", "1"]                              | 'stopped at offset 4: end of input\nexpected: "2"'
        "abc1x"         | ["abc", "1"]                              | 'stopped at offset 4: [x]\nexpected: "2"'
        "abc1xabc2"     | ["abc", "1"]                              | 'stopped at offset 4: [xabc2]\nexpected: "2"'
        "abc3"          | ["abc"]                                   | 'stopped at offset 3: [3]\nexpected: "1", "2"'
        "abc12abc"      | ["abc", "1", "2", "abc"]                  | 'stopped at offset 8: end of input\nexpected: "1", "2"'
        "abc12abc3"     | ["abc", "1", "2", "abc"]                  | 'stopped at offset 8: [3]\nexpected: "1", "2"'
        "abc12xabc2"    | ["abc", "1", "2"]                         | 'stopped at offset 5: [xabc2]\nexpected: "abc"'
        "abc12abc1"     | ["abc", "1", "2", "abc", "1"]             | 'stopped at offset 9: end of input\nexpected: "2"'
        "abc12abc1x"    | ["abc", "1", "2", "abc", "1"]             | 'stopped at offset 9: [x]\nexpected: "2"'
        "abc12abc12"    | ["abc", "1", "2", "abc", "1", "2"]        | 'stopped at offset 10: end of input\nexpected: "abc"'
        // TODO - missing an alternative
        "abc12abc12abc" | ["abc", "1", "2", "abc", "1", "2", "abc"] | 'stopped at offset 13: end of input\nexpected: "1", "2"'
    }

    @Unroll
    def "reports failure to match zero or more expression as group with common prefix with following expression - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.zeroOrMore(e1).group(), e2))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input           | tokens                | message
        ""              | []                    | 'stopped at offset 0: end of input\nexpected: "abc"'
        "abc"           | ["abc"]               | 'stopped at offset 3: end of input\nexpected: "1", "2"'
        "abc1"          | ["abc1"]              | 'stopped at offset 4: end of input\nexpected: "2"'
        "abc1x"         | ["abc1"]              | 'stopped at offset 4: [x]\nexpected: "2"'
        "abc1xabc2"     | ["abc1"]              | 'stopped at offset 4: [xabc2]\nexpected: "2"'
        "abc3"          | ["abc"]               | 'stopped at offset 3: [3]\nexpected: "1", "2"'
        "abc12abc"      | ["abc12", "abc"]      | 'stopped at offset 8: end of input\nexpected: "1", "2"'
        "abc12abc3"     | ["abc12", "abc"]      | 'stopped at offset 8: [3]\nexpected: "1", "2"'
        "abc12xabc2"    | ["abc12"]             | 'stopped at offset 5: [xabc2]\nexpected: "abc"'
        "abc12abc1"     | ["abc12", "abc1"]     | 'stopped at offset 9: end of input\nexpected: "2"'
        "abc12abc1x"    | ["abc12", "abc1"]     | 'stopped at offset 9: [x]\nexpected: "2"'
        "abc12abc12"    | ["abc12abc12"]        | 'stopped at offset 10: end of input\nexpected: "abc"'
        "abc12abc12abc" | ["abc12abc12", "abc"] | 'stopped at offset 13: end of input\nexpected: "1", "2"'
    }

    def "can parse one or more tokens"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.chars("abc")))
        parse(parser, "abcabc") == ["abc", "abc"]
        parse(parser, "abc") == ["abc"]
    }

    def "can parse one or more tokens as a group"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.chars("abc")).group())
        parse(parser, "abcabc") == ["abcabc"]
        parse(parser, "abc") == ["abc"]
    }

    def "can parse one or more sequence expressions"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))))
        parse(parser, "{abc}{abc}") == ["{", "abc", "}", "{", "abc", "}"]
        parse(parser, "{abc}") == ["{", "abc", "}"]
    }

    @Unroll
    def "reports failure to match one or more sequence expressions - #input"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input         | tokens                             | message
        ""            | []                                 | 'stopped at offset 0: end of input\nexpected: "{"'
        "{"           | ["{"]                              | 'stopped at offset 1: end of input\nexpected: "abc"'
        "{x"          | ["{"]                              | 'stopped at offset 1: [x]\nexpected: "abc"'
        "{abc"        | ["{", "abc"]                       | 'stopped at offset 4: end of input\nexpected: "}"'
        "{abc;"       | ["{", "abc"]                       | 'stopped at offset 4: [;]\nexpected: "}"'
        "{abc}x"      | ["{", "abc", "}"]                  | 'extra input at offset 5: [x]'
        "{abc}{"      | ["{", "abc", "}", "{"]             | 'stopped at offset 6: end of input\nexpected: "abc"'
        "{abc}{abc;"  | ["{", "abc", "}", "{", "abc"]      | 'stopped at offset 9: [;]\nexpected: "}"'
        "{abc}{abc}x" | ["{", "abc", "}", "{", "abc", "}"] | 'extra input at offset 10: [x]'
        "x"           | []                                 | 'stopped at offset 0: [x]\nexpected: "{"'
    }

    def "can parse one or more sequence expressions as a group"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.sequence(builder.chars("{"), builder.chars("abc"), builder.chars("}"))).group())
        parse(parser, "{abc}{abc}") == ["{abc}{abc}"]
        parse(parser, "{abc}") == ["{abc}"]
    }

    def "can parse one or more expressions with common prefix with following expression"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.oneOrMore(e1), e2))
        parse(parser, "abc1abc1abc2") == ["abc", "1", "abc", "1", "abc", "2"]
        parse(parser, "abc1abc2") == ["abc", "1", "abc", "2"]
    }

    def "can parse one of several alternative tokens"() {
        expect:
        def parser = builder.newParser(builder.oneOf(builder.chars("abc"), builder.chars("123")))
        parse(parser, "123") == ["123"]
        parse(parser, "abc") == ["abc"]
    }

    def "can parse one of several alternative tokens as a group"() {
        expect:
        def parser = builder.newParser(builder.oneOf(builder.chars("abc"), builder.chars("123")).group())
        parse(parser, "123") == ["123"]
        parse(parser, "abc") == ["abc"]
    }

    def "can parse one of several alternative sequence expressions"() {
        def e1 = builder.sequence(builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("2"), builder.chars("1"))

        expect:
        def parser = builder.newParser(builder.oneOf(e1, e2))
        parse(parser, "12") == ["1", "2"]
        parse(parser, "21") == ["2", "1"]
    }

    def "can parse one of several alternative sequence expressions as a group"() {
        def e1 = builder.sequence(builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("2"), builder.chars("1"))

        expect:
        def parser = builder.newParser(builder.oneOf(e1, e2).group())
        parse(parser, "12") == ["12"]
        parse(parser, "21") == ["21"]
    }

    def "can parse one of several alternative tokens with common prefix"() {
        expect:
        def parser = builder.newParser(builder.oneOf(builder.chars("abc1"), builder.chars("abc2"), builder.chars("abc")))
        parse(parser, "abc") == ["abc"]
        parse(parser, "abc1") == ["abc1"]
        parse(parser, "abc2") == ["abc2"]
    }

    def "can parse one of several alternative expressions with common prefix"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.oneOf(e1, e2))
        parse(parser, "abc1") == ["abc", "1"]
        parse(parser, "abc2") == ["abc", "2"]
    }

    @Unroll
    def "reports failure to match one of several alternative expressions - #input"() {
        def e1 = builder.sequence(builder.chars("ab"), builder.chars("12"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("1"))

        expect:
        def parser = builder.newParser(builder.oneOf(e1, e2))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input   | tokens       | message
        ""      | []           | 'stopped at offset 0: end of input\nexpected: "ab", "abc"'
        "a"     | []           | 'stopped at offset 0: [a]\nexpected: "ab", "abc"'
        "ab"    | ["ab"]       | 'stopped at offset 2: end of input\nexpected: "12"'
        "ab1"   | ["ab"]       | 'stopped at offset 2: [1]\nexpected: "12"'
        "ab13"  | ["ab"]       | 'stopped at offset 2: [13]\nexpected: "12"'
        "ab12x" | ["ab", "12"] | 'extra input at offset 4: [x]'
        "abd"   | ["ab"]       | 'stopped at offset 2: [d]\nexpected: "12"'
        "abc"   | ["abc"]      | 'stopped at offset 3: end of input\nexpected: "1"'
        "abc2"  | ["abc"]      | 'stopped at offset 3: [2]\nexpected: "1"'
        "abc1x" | ["abc", "1"] | 'extra input at offset 4: [x]'
        "adc"   | []           | 'stopped at offset 0: [adc]\nexpected: "ab", "abc"'
    }

    @Unroll
    def "reports failure to match one of several alternative expressions with common prefix and suffix - #input"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"), builder.chars("2"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.oneOf(e1, e2))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input   | tokens       | message
        ""      | []           | 'stopped at offset 0: end of input\nexpected: "abc"'
        "ab"    | []           | 'stopped at offset 0: [ab]\nexpected: "abc"'
        "abc"   | ["abc"]      | 'stopped at offset 3: end of input\nexpected: "1", "2"'
        "abc3"  | ["abc"]      | 'stopped at offset 3: [3]\nexpected: "1", "2"'
        "abc1"  | ["abc", "1"] | 'stopped at offset 4: end of input\nexpected: "2"'
        "abc1x" | ["abc", "1"] | 'stopped at offset 4: [x]\nexpected: "2"'
        "abc2"  | ["abc", "2"] | 'stopped at offset 4: end of input\nexpected: "2"'
        "abc2x" | ["abc", "2"] | 'stopped at offset 4: [x]\nexpected: "2"'
    }

    def "can parse one of several alternative expressions with common prefix with following expression"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))
        def e3 = builder.sequence(builder.chars("abc"), builder.chars("3"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.oneOf(e1, e2), e3))
        parse(parser, "abc1abc3") == ["abc", "1", "abc", "3"]
        parse(parser, "abc2abc3") == ["abc", "2", "abc", "3"]
    }

    def "can parse a sequence of optional tokens"() {
        def e1 = builder.optional(builder.chars("abc"))
        def e2 = builder.optional(builder.chars("123"))

        expect:
        def parser = builder.newParser(builder.sequence(e1, e2))
        parse(parser, "abc123") == ["abc", "123"]
        parse(parser, "abc") == ["abc"]
        parse(parser, "123") == ["123"]
        parse(parser, "") == []
    }

    def "can parse a sequence of optional expressions with common prefix"() {
        def e1 = builder.optional(builder.sequence(builder.chars("abc"), builder.chars("1")))
        def e2 = builder.optional(builder.sequence(builder.chars("abc"), builder.chars("2")))

        expect:
        def parser = builder.newParser(builder.sequence(e1, e2))
        parse(parser, "abc1abc2") == ["abc", "1", "abc", "2"]
        parse(parser, "abc1") == ["abc", "1"]
        parse(parser, "abc2") == ["abc", "2"]
        parse(parser, "") == []
    }

    def "can parse optional expression with common prefix with following expression"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.sequence(builder.optional(e1), e2))
        parse(parser, "abc1abc2") == ["abc", "1", "abc", "2"]
        parse(parser, "abc2") == ["abc", "2"]
    }

    def List<String> parse(Parser parser, String str) {
        def visitor = parser.parse(str, new CollectingVisitor())
        assert visitor.failure == null
        return visitor.result
    }

    def CollectingVisitor fail(Parser parser, String str) {
        def visitor = parser.parse(str, new CollectingVisitor())
        assert visitor.failure != null
        return visitor
    }
}
