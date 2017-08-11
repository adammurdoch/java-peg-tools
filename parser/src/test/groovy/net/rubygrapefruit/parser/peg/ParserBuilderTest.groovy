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
        result.failure == "extra input: offset 3 '123'"
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
        ""     | []     | "stopped at: end of input"
        "ab"   | []     | "stopped at: offset 0 'ab'"
        "abd"  | []     | "stopped at: offset 0 'abd'"
        "ABC"  | []     | "stopped at: offset 0 'ABC'"
        "123"  | []     | "stopped at: offset 0 '123'"
        "1abc" | []     | "stopped at: offset 0 '1abc'"
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
        ""    | []     | "stopped at: end of input"
        "X"   | []     | "stopped at: offset 0 'X'"
        "y"   | []     | "stopped at: offset 0 'y'"
        "yx"  | []     | "stopped at: offset 0 'yx'"
    }

    def "can parse a sequence of tokens"() {
        expect:
        def parser = builder.newParser(builder.sequence(builder.chars("abc"), builder.chars("123")))
        parse(parser, "abc123") == ["abc", "123"]
    }

    def "can group a sequence of tokens into a single token"() {
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

    @Unroll
    def "reports failure to match a sequence of tokens - #input"() {
        expect:
        def parser = builder.newParser(builder.sequence(builder.chars("abc"), builder.chars("123")))
        def result = fail(parser, input)
        result.result == tokens
        result.failure == message

        where:
        input     | tokens  | message
        ""        | []      | "stopped at: end of input"
        "abc"     | ["abc"] | "stopped at: end of input"
        "abc124"  | ["abc"] | "stopped at: offset 3 '124'"
        "1abc123" | []      | "stopped at: offset 0 '1abc123'"
        "abcx123" | ["abc"] | "stopped at: offset 3 'x123'"
        "abc1123" | ["abc"] | "stopped at: offset 3 '1123'"
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
        ""          | []                  | "stopped at: end of input"
        "ab"        | []                  | "stopped at: offset 0 'ab'"
        "abc"       | ["abc"]             | "stopped at: end of input"
        "abc2"      | ["abc"]             | "stopped at: offset 3 '2'"
        "abc124"    | ["abc", "1"]        | "stopped at: offset 4 '24'"
        "abc1abc"   | ["abc", "1", "abc"] | "stopped at: end of input"
        "abc1abc1"  | ["abc", "1", "abc"] | "stopped at: offset 7 '1'"
        "1abc1abc2" | []                  | "stopped at: offset 0 '1abc1abc2'"
    }

    def "can parse optional token"() {
        expect:
        def parser = builder.newParser(builder.optional(builder.chars("abc")))
        parse(parser, "abc") == ["abc"]
        parse(parser, "") == []
    }

    def "can parse zero or more tokens"() {
        expect:
        def parser = builder.newParser(builder.zeroOrMore(builder.chars("abc")))
        parse(parser, "abcabc") == ["abc", "abc"]
        parse(parser, "abc") == ["abc"]
        parse(parser, "") == []
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

    def "can parse one or more tokens"() {
        expect:
        def parser = builder.newParser(builder.oneOrMore(builder.chars("abc")))
        parse(parser, "abcabc") == ["abc", "abc"]
        parse(parser, "abc") == ["abc"]
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
