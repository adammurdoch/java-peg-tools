package net.rubygrapefruit.parser.java

import spock.lang.Specification

class JavaParserTest extends Specification {
    def "can parse Java class definition"() {
        expect:
        new JavaParser().parse("class Thing { }") == ["class", " ", "Thing", " ", "{", " ", "}"]
    }
}
