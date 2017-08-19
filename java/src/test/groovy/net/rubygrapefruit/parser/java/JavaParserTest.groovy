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
        def r0 = fail("")
        r0.result == []
        r0.failure == 'stopped at offset 0: end of input\nexpected: "\n", " ", "abstract", "class", "import", "interface", "package", "public"'

        def r1 = fail(" Thing { }")
        r1.result == [" "]
        r1.failure == 'stopped at offset 1: [Thing { }]\nexpected: "\n", " ", "abstract", "class", "import", "interface", "package", "public"'

        def r2 = fail("class x")
        r2.result == ["class", " ", "x"]
        r2.failure == 'stopped at offset 7: end of input\nexpected: "\n", " ", "{", {letter}'

        def r2_1 = fail("class x ")
        r2_1.result == ["class", " ", "x", " "]
        r2_1.failure == 'stopped at offset 8: end of input\nexpected: "\n", " ", "extends", "implements", "{"'

        def r3 = fail("class Thing extends { }")
        r3.result == ["class", " ", "Thing", " ", "extends", " "]
        r3.failure == 'stopped at offset 20: [{ }]\nexpected: "\n", " ", {letter}'

        def r3_1 = fail("class Thing implements { }")
        r3_1.result == ["class", " ", "Thing", " ", "implements", " "]
        r3_1.failure == 'stopped at offset 23: [{ }]\nexpected: "\n", " ", {letter}'

        def r4 = fail("class Thing implements A extends B { }")
        r4.result == ["class", " ", "Thing", " ", "implements", " ", "A", " "]
        r4.failure == 'stopped at offset 25: [extends B { }]\nexpected: "\n", " ", ",", "{"'

        def r5 = fail("class Thing implements A implements B { }")
        r5.result == ["class", " ", "Thing", " ", "implements", " ", "A", " "]
        r5.failure == 'stopped at offset 25: [implements B { }]\nexpected: "\n", " ", ",", "{"'

        def r6 = fail("class Thing extends A, B { }")
        r6.result == ["class", " ", "Thing", " ", "extends", " ", "A"]
        r6.failure == 'stopped at offset 21: [, B { }]\nexpected: "\n", " ", "{", {letter}'

        // TODO - should really complain that interface can't be abstract
        // TODO - shouldn't offer abstract as an alternative
        def r7 = fail("abstract interface Thing extends { }")
        r7.result == ["abstract", " "]
        r7.failure == 'stopped at offset 9: [interface Thing exte]\nexpected: "abstract", "class", "public"'

        // TODO - missing 'extends' as alternatives
        def r8 = fail("interface Thing implements A { }")
        r8.result == ["interface", " ", "Thing", " "]
        r8.failure == 'stopped at offset 16: [implements A { }]\nexpected: "\n", " ", "extends", "{"'

        def r9 = fail("x")
        r9.result == []
        r9.failure == 'stopped at offset 0: [x]\nexpected: "\n' + '", " ", "abstract", "class", "import", "interface", "package", "public"'

        // TODO - 'a.b' and '.' should be in same token
        def r10 = fail("package a.b.{")
        r10.result == ["package", " ", "a.b", "."]
        r10.failure == 'stopped at offset 12: [{]\nexpected: {letter}'

        def r11 = fail("package a.b import c.d")
        r11.result == ["package", " ", "a.b", " "]
        r11.failure == 'stopped at offset 12: [import c.d]\nexpected: "\n", " ", ";"'

        def r12 = fail("package a.b; import a.b{}")
        r12.result == ["package", " ", "a.b", ";", " ", "import", " ", "a.b"]
        r12.failure == 'stopped at offset 23: [{}]\nexpected: "\n", " ", ".", ";"'

        // TODO - 'a.b' and '.' should be same token
        def r13 = fail("package a.b; import a.b.%; class Thing { }")
        r13.result == ["package", " ", "a.b", ";", " ", "import", " ", "a.b", "."]
        r13.failure == 'stopped at offset 24: [%; class Thing { }]\nexpected: {letter}'

        // TODO - not quite right, should complain about an unexpected identifier
        def r14 = fail("packageimportclass")
        r14.result == ["package"]
        r14.failure == 'stopped at offset 7: [importclass]\nexpected: "\n", " "'

        // TODO - should complain about 'import' keyword instead of accepting it
        def r15 = fail("package import a;")
        r15.result == ["package", " ", "import", " "]
        r15.failure == 'stopped at offset 15: [a;]\nexpected: "\n", " ", ";"'

        // TODO - missing '.' as an alternative
        def r16 = fail("package a")
        r16.result == ["package", " ", "a"]
        r16.failure == 'stopped at offset 9: end of input\nexpected: "\n", " ", ";", {letter}'

        def r17 = fail("import a; ")
        r17.result == ["import", " ", "a", ";", " "]
        r17.failure == 'stopped at offset 10: end of input\nexpected: "abstract", "class", "import", "interface", "public"'

        // TODO - shouldn't offer public as an alternative
        def r18 = fail("public ")
        r18.result == ["public", " "]
        r18.failure == 'stopped at offset 7: end of input\nexpected: "\n", " ", "abstract", "class", "interface", "public"'
    }

    def List<String> parse(String str) {
        return parser.parse(str, new CollectingVisitor()).result
    }

    def CollectingVisitor fail(String str) {
        return parser.parse(str, new CollectingVisitor())
    }
}
