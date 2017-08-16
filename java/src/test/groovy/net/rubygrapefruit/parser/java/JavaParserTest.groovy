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
        def r1 = fail(" Thing { }")
        r1.result == [" "]
        r1.failure == "stopped at offset 1: [Thing { }]"

        def r2 = fail("class x")
        r2.result == ["class", " ", "x"]
        r2.failure == "stopped at offset 7: end of input"

        // TODO - incomplete
        def r3 = fail("class Thing extends { }")
        r3.result == ["class", " ", "Thing", " "]
        r3.failure == "stopped at offset 12: [extends { }]"

        def r4 = fail("class Thing implements A extends B { }")
        r4.result == ["class", " ", "Thing", " ", "implements", " ", "A", " "]
        r4.failure == "stopped at offset 25: [extends B { }]"

        def r5 = fail("class Thing implements A implements B { }")
        r5.result == ["class", " ", "Thing", " ", "implements", " ", "A", " "]
        r5.failure == "stopped at offset 25: [implements B { }]"

        def r6 = fail("class Thing extends A, B { }")
        r6.result == ["class", " ", "Thing", " ", "extends", " ", "A"]
        r6.failure == "stopped at offset 21: [, B { }]"

        // TODO - should really complain that interface can't be abstract
        def r7 = fail("abstract interface Thing extends { }")
        r7.result == ["abstract", " "]
        r7.failure == "stopped at offset 9: [interface Thing exte]"

        def r8 = fail("interface Thing implements A { }")
        r8.result == ["interface", " ", "Thing", " "]
        r8.failure == "stopped at offset 16: [implements A { }]"

        def r9 = fail("x")
        r9.result == []
        r9.failure == "stopped at offset 0: [x]"

        // TODO - incomplete
        def r10 = fail("package a.b.{")
        r10.result == []
        r10.failure == "stopped at offset 0: [package a.b.{]"

        // TODO - incomplete
        def r11 = fail("package a.b import a.b")
        r11.result == []
        r11.failure == "stopped at offset 0: [package a.b import a]"

        // TODO - incomplete
        def r12 = fail("package a.b; import a.b{}")
        r12.result == ["package", " ", "a.b", ";", " "]
        r12.failure == "stopped at offset 23: [{}]"

        // TODO - incomplete (should have consumed the '.')
        def r13 = fail("package a.b; import a.b.%; class Thing { }")
        r13.result == ["package", " ", "a.b", ";", " "]
        r13.failure == "stopped at offset 23: [.%; class Thing { }]"

        // TODO - incomplete
        def r14 = fail("packageimportclass")
        r14.result == []
        r14.failure == "stopped at offset 0: [packageimportclass]"
    }

    def List<String> parse(String str) {
        return parser.parse(str, new CollectingVisitor()).result
    }

    def CollectingVisitor fail(String str) {
        return parser.parse(str, new CollectingVisitor())
    }
}
