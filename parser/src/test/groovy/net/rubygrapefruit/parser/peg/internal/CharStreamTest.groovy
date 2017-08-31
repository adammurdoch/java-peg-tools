package net.rubygrapefruit.parser.peg.internal

import spock.lang.Specification

class CharStreamTest extends Specification {
    def "empty stream"() {
        expect:
        def stream = new CharStream("")
        def start = stream.current()
        start.atEnd
        start.offset == 0
        start.line == 1
        start.column == 1
        start.currentLine == ""

        def end = stream.end()
        end.offset == 0
        end.line == 1
        end.column == 1
        end.currentLine == ""
    }

    def "has start position"() {
        expect:
        def stream = new CharStream("abc")
        def current = stream.current()
        !current.atEnd
        current.offset == 0
        current.line == 1
        current.column == 1
        current.currentLine == "abc"
    }

    def "has end position"() {
        expect:
        def stream = new CharStream("abc")
        def end = stream.end()
        end.atEnd
        end.offset == 3
        end.line == 1
        end.column == 4
        end.currentLine == "abc"
    }

    def "updates position as characters are consumed"() {
        expect:
        def stream = new CharStream("abc")
        stream.consume("ab")
        def current = stream.current()
        current.offset == 2
        current.line == 1
        current.column == 3
    }

    def "groups input into lines as characters are consumed"() {
        expect:
        def stream = new CharStream("abc\n123\r\ndef\n\n\r\n123")

        stream.consume("abc")

        def p1 = stream.current()
        p1.offset == 3
        p1.line == 1
        p1.column == 4
        p1.currentLine == "abc"

        stream.consumeOne()

        def p2 = stream.current()
        p2.offset == 4
        p2.line == 2
        p2.column == 1
        p2.currentLine == "123"

        stream.consume("12")

        def p3 = stream.current()
        p3.offset == 6
        p3.line == 2
        p3.column == 3
        p3.currentLine == "123"

        stream.consumeOne()

        def p4 = stream.current()
        p4.offset == 7
        p4.line == 2
        p4.column == 4
        p4.currentLine == "123"

        stream.consumeOne()

        def p5 = stream.current()
        p5.offset == 8
        p5.line == 2
        p5.column == 5
        p5.currentLine == "123"

        stream.consumeOne()

        def p6 = stream.current()
        p6.offset == 9
        p6.line == 3
        p6.column == 1
        p6.currentLine == "def"

        stream.consume("def")
        stream.consumeOne()

        def p7 = stream.current()
        p7.offset == 13
        p7.line == 4
        p7.column == 1
        p7.currentLine == ""

        stream.consumeOne()
        def p8 = stream.current()
        p8.offset == 14
        p8.line == 5
        p8.column == 1
        p8.currentLine == ""

        stream.consumeOne()
        def p9 = stream.current()
        p9.offset == 15
        p9.line == 5
        p9.column == 2
        p9.currentLine == ""
    }

    def "single cr character does not form line ending but cr followed by nl does"() {
        expect:
        def stream = new CharStream("\r\r\n\r\r")

        def p1 = stream.current()
        p1.offset == 0
        p1.line == 1
        p1.column == 1
        p1.currentLine == "\r"

        stream.consumeOne()

        def p2 = stream.current()
        p2.offset == 1
        p2.line == 1
        p2.column == 2
        p2.currentLine == "\r"

        stream.consumeOne()

        def p3 = stream.current()
        p3.offset == 2
        p3.line == 1
        p3.column == 3
        p3.currentLine == "\r"

        stream.consumeOne()

        def p4 = stream.current()
        p4.offset == 3
        p4.line == 2
        p4.column == 1
        p4.currentLine == "\r\r"

        stream.consumeOne()

        def p5 = stream.current()
        p5.offset == 4
        p5.line == 2
        p5.column == 2
        p5.currentLine == "\r\r"

        stream.consumeOne()

        def p6 = stream.current()
        p6.offset == 5
        p6.line == 2
        p6.column == 3
        p6.currentLine == "\r\r"
    }

    def "calculates line and col of end"() {
        expect:
        def s1 = new CharStream("abc\n123\r\ndef\n\n123")
        def p1 = s1.end()
        p1.offset == 17
        p1.line == 5
        p1.column == 4
        p1.currentLine == "123"

        def s2 = new CharStream("abc\n123\r\ndef\n\n123")
        s2.consume("abc\n123\r\n")
        s2.current().line == 3

        def p2 = s2.end()
        p2.offset == 17
        p2.line == 5
        p2.column == 4
        p2.currentLine == "123"

        def s3 = new CharStream("abc\n123\r\ndef\n\n123")
        s3.consume("abc\n123\r\ndef\n\n123")
        s3.current().line == 5

        def p3 = s3.end()
        p3.offset == 17
        p3.line == 5
        p3.column == 4
        p3.currentLine == "123"
    }

}
