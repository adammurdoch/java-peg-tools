package net.rubygrapefruit.parser.peg

import net.rubygrapefruit.parser.peg.visitor.CollectingVisitor
import spock.lang.Specification

class ParserBuilderTest extends Specification {
    def builder = new ParserBuilder()

    def "can parse a string token"() {
        expect:
        def parser = builder.newParser(builder.chars("abc"))
        parse(parser, "abc") == ["abc"]
    }

    def "reports failure to match string token"() {
        expect:
        def parser = builder.newParser(builder.chars("abc"))
        parse(parser, "") == []
        parse(parser, "ab") == []
        parse(parser, "abd") == []
        parse(parser, "ABC") == []
        parse(parser, "abc1") == ["abc"]
        parse(parser, "123") == []
        parse(parser, "1abc") == []
    }

    def "can parse a string character"() {
        expect:
        def parser = builder.newParser(builder.singleChar(";" as char))
        parse(parser, ";") == [";"]
    }

    def "reports failure to match a string character"() {
        expect:
        def parser = builder.newParser(builder.singleChar("x" as char))
        parse(parser, "") == []
        parse(parser, "X") == []
        parse(parser, "y") == []
        parse(parser, "yx") == []
        parse(parser, "xy") == ["x"]
    }

    def "can parse a sequence of tokens"() {
        expect:
        def parser = builder.newParser(builder.sequence(builder.chars("abc"), builder.chars("123")))
        parse(parser, "abc123") == ["abc", "123"]
    }

    def "reports failure to match a sequence of tokens"() {
        expect:
        def parser = builder.newParser(builder.sequence(builder.chars("abc"), builder.chars("123")))
        parse(parser, "") == []
        parse(parser, "abc") == ["abc"]
        parse(parser, "abc124") == ["abc"]
        parse(parser, "1abc123") == []
        parse(parser, "abc123x") == ["abc", "123"]
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
        def parser = builder.newParser(builder.anyOf(builder.chars("abc"), builder.chars("123")))
        parse(parser, "123") == ["123"]
        parse(parser, "abc") == ["abc"]
    }

    def "can parse one of several alternative tokens with common prefix"() {
        expect:
        def parser = builder.newParser(builder.anyOf(builder.chars("abc1"), builder.chars("abc2"), builder.chars("abc")))
        parse(parser, "abc") == ["abc"]
        parse(parser, "abc1") == ["abc1"]
        parse(parser, "abc2") == ["abc2"]
    }

    def "can parse one of several alternative expressions with common prefix"() {
        def e1 = builder.sequence(builder.chars("abc"), builder.chars("1"))
        def e2 = builder.sequence(builder.chars("abc"), builder.chars("2"))

        expect:
        def parser = builder.newParser(builder.anyOf(e1, e2))
        parse(parser, "abc1") == ["abc", "1"]
        parse(parser, "abc2") == ["abc", "2"]
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

    def List<String> parse(Parser parser, String str) {
        return parser.parse(str, new CollectingVisitor()).result
    }
}
