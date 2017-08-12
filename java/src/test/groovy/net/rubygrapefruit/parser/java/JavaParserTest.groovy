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
        parse("public class Thing { }") == ["public", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("abstract class Thing { }") == ["abstract", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("abstract public class Thing { }") == ["abstract", " ", "public", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("public abstract class Thing { }") == ["public", " ", "abstract", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("class Thing extends OtherThing { }") == ["class", " ", "Thing", " ", "extends", " ", "OtherThing", " ", "{", " ", "}"]
        parse("class Thing implements OtherThing { }") == ["class", " ", "Thing", " ", "implements", " ", "OtherThing", " ", "{", " ", "}"]
        parse("class Thing implements A, B { }") == ["class", " ", "Thing", " ", "implements", " ", "A", ",", " ", "B", " ", "{", " ", "}"]
        parse("class Thing extends A implements B { }") == ["class", " ", "Thing", " ", "extends", " ", "A", " ", "implements", " ", "B", " ", "{", " ", "}"]
        parse("class Thing extends A implements B,C{ }") == ["class", " ", "Thing", " ", "extends", " ", "A", " ", "implements", " ", "B", ",", "C", "{", " ", "}"]
    }

    def "can parse Java interface definition"() {
        expect:
        parse("interface Thing { }") == ["interface", " ", "Thing", " ", "{", " ", "}"]
        parse("public interface Thing { }") == ["public", " ", "interface", " ", "Thing", " ", "{", " ", "}"]
        parse("interface Thing extends OtherThing { }") == ["interface", " ", "Thing", " ", "extends", " ", "OtherThing", " ", "{", " ", "}"]
        parse("interface Thing extends A, B { }") == ["interface", " ", "Thing", " ", "extends", " ", "A", ",", " ", "B", " ", "{", " ", "}"]
        parse("interface Thing extends A,B{ }") == ["interface", " ", "Thing", " ", "extends", " ", "A", ",", "B", "{", " ", "}"]
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
        parse("class Thing implements A extends B { }") == ["class", " ", "Thing", " ", "implements", " ", "A", " "]
        parse("class Thing implements A implements B { }") == ["class", " ", "Thing", " ", "implements", " ", "A", " "]
        parse("class Thing extends A, B { }") == ["class", " ", "Thing", " ", "extends", " ", "A"]
        parse("abstract interface Thing extends { }") == ["abstract", " "]
        parse("interface Thing implements A { }") == ["interface", " ", "Thing", " "]
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
