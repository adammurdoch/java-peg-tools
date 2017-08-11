package net.rubygrapefruit.parser.java

import net.rubygrapefruit.parser.peg.visitor.CollectingVisitor
import spock.lang.Specification

class JavaParserTest extends Specification {
    def parser = new JavaParser()

    def "can parse Java class definition"() {
        expect:
        parse("class Thing { }") == ["class", " ", "Thing", " ", "{", " ", "}"]
        parse("class Thing{}") == ["class", " ", "Thing", "{", "}"]
        parse("""

  class  Thing
  {

  }
""") == ["\n\n  ", "class", "  ", "Thing", "\n  ", "{", "\n\n  ", "}", "\n"]
    }

    def "can parse optional package declaration"() {
        expect:
        parse("package thing; class Thing { }") == ["package", " ", "thing", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("package a.b.c; class Thing { }") == ["package", " ", "a.b.c", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("  package   a.b.c   ; class Thing { }") == ["  ", "package", "   ", "a.b.c", "   ", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
    }

    def "can parse optional import statements"() {
        expect:
        parse("import thing; class Thing { }") == ["import", " ", "thing", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("import a.b.c; class Thing { }") == ["import", " ", "a.b.c", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("import a.b.c.*; class Thing { }") == ["import", " ", "a.b.c.*", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("import thing; import a.b; import a.b.c.*; class Thing { }") == ["import", " ", "thing", ";", " ", "import", " ", "a.b", ";", " ", "import", " ", "a.b.c.*", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
    }

    def "stops parsing on first error"() {
        expect:
        parse(" Thing { }") == [" "]
        parse("class x") == ["class", " ", "x"]
        parse("class Thing extends { }") == ["class", " ", "Thing", " "]
        parse("x") == []
        parse("package a.b.{") == []
        parse("package a.b import a.b") == []
        parse("package a.b; import a.b{}") == ["package", " ", "a.b", ";", " "]
        parse("package a.b; import a.b.%; class Thing { }") == ["package", " ", "a.b", ";", " "]
        parse("packageimportclass") == []
    }

    def List<String> parse(String str) {
        return parser.parse(str, new CollectingVisitor()).result
    }
}
