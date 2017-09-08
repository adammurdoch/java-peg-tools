package net.rubygrapefruit.parser.peg

import spock.lang.Unroll

class ReferenceExpressionTest extends AbstractParserTest {
    def "parsing fails when reference has no value"() {
        given:
        def ref = builder.reference()
        def parser = builder.newParser(builder.oneOrMore(ref))

        when:
        parser.parse("123", Stub(TokenVisitor))

        then:
        def e = thrown(IllegalStateException)
        e.message == 'No target has been set for reference expression.'
    }

    def "cannot change target of reference after it has been used"() {
        def visitor = Mock(TokenVisitor)

        given:
        def ref = builder.reference()
        ref.set(builder.letter())
        def parser = builder.newParser(builder.oneOrMore(ref))

        when:
        parser.parse("abc", visitor)

        then:
        def e = thrown(IllegalStateException)
        e.message == 'Cannot set the target for a reference expression after the reference has been used.'

        1 * visitor.token(_, _) >> {
            ref.set(builder.chars("abc"))
        }
    }

    def "can create a reference to another expression"() {
        expect:
        def ref = builder.reference()
        def e = builder.oneOrMore(builder.letter()).group()
        ref.set(e)
        def parser = builder.newParser(ref)

        def result = parse(parser, "abc")
        result.tokens == ["abc"]
        result.values == [e]
    }

    def "can parse reference to sequence as a group"() {
        expect:
        def ref = builder.reference()
        def e = builder.sequence(builder.letter(), builder.letter(), builder.letter())
        ref.set(e)
        def group = ref.group()
        def parser = builder.newParser(group)

        def result = parse(parser, "abc")
        result.tokens == ["abc"]
        result.values == [group]
    }

    def "can parse sequence of references"() {
        expect:
        def ref = builder.reference()
        def e = builder.sequence(ref, ref, ref)
        def terminal = builder.letter()
        ref.set(terminal)
        def parser = builder.newParser(e)

        def result = parse(parser, "abc")
        result.tokens == ["a", "b", "c"]
        result.values == [terminal, terminal, terminal]
    }

    def "can parse recursive expression"() {
        expect:
        def ref = builder.reference()
        def leftDelim = builder.chars("{")
        def rightDelim = builder.chars("}")
        def block = builder.sequence(leftDelim, ref, rightDelim)
        def identifier = builder.letter()
        def expression = builder.oneOf(block, identifier)
        ref.set(expression)
        def parser = builder.newParser(expression)

        def r1 = parse(parser, "a")
        r1.tokens == ["a"]
        r1.values == [identifier]

        def r2 = parse(parser, "{a}")
        r2.tokens == ["{", "a", "}"]
        r2.values == [leftDelim, identifier, rightDelim]

        def r3 = parse(parser, "{{a}}")
        r3.tokens == ["{", "{", "a", "}", "}"]
        r3.values == [leftDelim, leftDelim, identifier, rightDelim, rightDelim]
    }

    @Unroll
    def "reports failure to match recursive expression - #input"() {
        def ref = builder.reference()
        def leftDelim = builder.chars("{")
        def rightDelim = builder.chars("}")
        def block = builder.sequence(leftDelim, ref, rightDelim)
        def identifier = builder.letter()
        def expression = builder.oneOf(block, identifier)
        ref.set(expression)

        expect:
        def parser = builder.newParser(expression)
        def result = fail(parser, input)
        result.tokens == tokens
        result.failure == message

        where:
        input   | tokens               | message
        ""      | []                   | '''line 1: expected "{" or letter

^'''
        "abc"   | ["a"]                | '''line 1: unexpected characters
abc
 ^'''
        "{"     | ["{"]                | '''line 1: expected "{" or letter
{
 ^'''
        "{1"    | ["{"]                | '''line 1: expected "{" or letter
{1
 ^'''
        "{}"    | ["{"]                | '''line 1: expected "{" or letter
{}
 ^'''
        "{{{{"  | ["{", "{", "{", "{"] | '''line 1: expected "{" or letter
{{{{
    ^'''
        "{ab"   | ["{", "a"]           | '''line 1: expected "}"
{ab
  ^'''
        "{{a}"  | ["{", "{", "a", "}"] | '''line 1: expected "}"
{{a}
    ^'''
        "{{a}a" | ["{", "{", "a", "}"] | '''line 1: expected "}"
{{a}a
    ^'''
        "{{1}"  | ["{", "{"]           | '''line 1: expected "{" or letter
{{1}
  ^'''
    }
}
