package net.rubygrapefruit.parser.peg

import spock.lang.Unroll

class BackReferenceExpressionTest extends AbstractParserTest {
    def "fails when back reference is used outside owning expression"() {
        expect: false
    }

    def "parses back reference"() {
        expect:
        def e = builder.chars("abc")
        def ref = builder.backReference(e)
        def root = ref.followedBy(ref.value)
        def parser = builder.newParser(root)
        def r = parse(parser, "abcabc")
        r.tokens == ["abc", "abc"]
        r.values == [e, ref.value]
    }

    def "parses back reference in nested expression"() {
        expect:
        def e = builder.letter()
        def e2 = builder.oneOrMore(e)
        def ref = builder.backReference(e2)
        def delim = builder.singleChar(';' as char)
        def root = ref.followedBy(builder.sequence(delim, builder.zeroOrMore(ref.value), delim))
        def parser = builder.newParser(root)

        def r = parse(parser, "ab;;")
        r.tokens == ["a", "b", ";", ";"]
        r.values == [e, e, delim, delim]

        def r2 = parse(parser, "ab;abab;")
        r2.tokens == ["a", "b", ";", "ab", "ab", ";"]
        r2.values == [e, e, delim, ref.value, ref.value, delim]

        def r3 = parse(parser, "a;aa;")
        r3.tokens == ["a", ";", "a", "a", ";"]
        r3.values == [e, delim, ref.value, ref.value, delim]
    }

    def "parses back reference in recursive expression"() {
        expect:
        def d1 = builder.chars("-")
        def d2 = builder.chars(".")
        def e1 = builder.letter()
        def e2 = builder.oneOrMore(e1).group()
        def delims = builder.oneOf(d1, d2)
        def ref = builder.backReference(delims)
        def root = builder.reference()
        def expr = ref.followedBy(builder.sequence(builder.oneOf(e2, root), ref.value))
        root.set(expr)
        def parser = builder.newParser(root)

        def r = parse(parser, "-abc-")
        r.tokens == ["-", "abc", "-"]
        r.values == [d1, e2, ref.value]

        def r2 = parse(parser, "--abc--")
        r2.tokens == ["-", "-", "abc", "-", "-"]
        r2.values == [d1, d1, e2, ref.value, ref.value]

        def r3 = parse(parser, "-.-a-.-")
        r3.tokens == ["-", ".", "-", "a", "-", ".", "-"]
        r3.values == [d1, d2, d1, e2, ref.value, ref.value, ref.value]
    }

    def "parses back reference as a group"() {
        expect: false
    }

    @Unroll
    def "reports failure to parse back reference - #input"() {
        def e = builder.chars("abc")
        def ref = builder.backReference(e)
        def expression = ref.followedBy(ref.value)

        expect:
        def parser = builder.newParser(expression)
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input     | tokens         | message
        ""        | []             | '''line 1: expected "abc"

^'''
        "ab"      | []             | '''line 1: expected "abc"
ab
^'''
        "abc"     | ["abc"]        | '''line 1: expected "abc"
abc
   ^'''
        "abc1"    | ["abc"]        | '''line 1: expected "abc"
abc1
   ^'''
        "abcab"   | ["abc"]        | '''line 1: expected "abc"
abcab
   ^'''
        "abcabc2" | ["abc", "abc"] | '''line 1: unexpected characters
abcabc2
      ^'''
    }

    def "reports failure to parse nested back reference"() {
        expect: false
    }
}
