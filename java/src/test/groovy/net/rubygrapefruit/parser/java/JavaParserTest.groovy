package net.rubygrapefruit.parser.java

import spock.lang.Specification

class JavaParserTest extends Specification {
    def parser = new JavaParser()

    def "can parse Java class definition"() {
        expect:
        parser.parse("class Thing { }") == ["class", " ", "Thing", " ", "{", " ", "}"]
        parser.parse("class Thing{}") == ["class", " ", "Thing", "{", "}"]
        parser.parse("""

  class  Thing
  {

  }
""") == ["\n\n  ", "class", "  ", "Thing", "\n  ", "{", "\n\n  ", "}", "\n"]
    }

    def "can parse optional package declaration"() {
        expect:
        parser.parse("package thing; class Thing { }") == ["package", " ", "thing", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parser.parse("package a.b.c; class Thing { }") == ["package", " ", "a.b.c", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parser.parse("  package   a.b.c   ; class Thing { }") == ["  ", "package", "   ", "a.b.c", "   ", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
    }

    def "stops parsing on first error"() {
        expect:
        parser.parse(" Thing { }") == [" "]
        parser.parse("class x") == ["class", " ", "x"]
        parser.parse("class Thing extends { }") == ["class", " ", "Thing", " "]
    }
}
