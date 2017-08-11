package net.rubygrapefruit.parser.java

import spock.lang.Specification

class JavaParserTest extends Specification {
    def "can create Java parser"() {
        expect:
        new JavaParser()
    }
}
