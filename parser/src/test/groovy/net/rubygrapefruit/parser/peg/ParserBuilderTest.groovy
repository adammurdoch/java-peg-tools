package net.rubygrapefruit.parser.peg

import spock.lang.Specification

class ParserBuilderTest extends Specification {
    def "can create empty parser"() {
        expect:
        new ParserBuilder()
    }
}
