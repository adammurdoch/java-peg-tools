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

        parse("class Thing<T>{}") == ["class", " ", "Thing", "<", "T", ">", "{", "}"]
        // TODO - should not require whitespace before `extends`
        parse("class Thing<T> extends A{}") == ["class", " ", "Thing", "<", "T", ">", " ", "extends", " ", "A", "{", "}"]
        // TODO - should not require whitespace before `implements`
        parse("class Thing<T> implements A,B{}") == ["class", " ", "Thing", "<", "T", ">", " ", "implements", " ", "A", ",", "B", "{", "}"]
        parse("class Thing  < T/* */>  {}") == ["class", " ", "Thing", "  ", "<", " ", "T", "/* */", ">", "  ", "{", "}"]
    }

    def "can parse Java interface definition"() {
        expect:
        parse("interface Thing { }") == ["interface", " ", "Thing", " ", "{", " ", "}"]
        parse("public interface Thing { }") == ["public", " ", "interface", " ", "Thing", " ", "{", " ", "}"]
        parse("interface Thing extends OtherThing { }") == ["interface", " ", "Thing", " ", "extends", " ", "OtherThing", " ", "{", " ", "}"]
        parse("interface Thing extends A, B { }") == ["interface", " ", "Thing", " ", "extends", " ", "A", ",", " ", "B", " ", "{", " ", "}"]
        parse("interface Thing extends A,B{ }") == ["interface", " ", "Thing", " ", "extends", " ", "A", ",", "B", "{", " ", "}"]

        parse("interface Thing<T>{ }") == ["interface", " ", "Thing", "<", "T", ">", "{", " ", "}"]
        // TODO - should not require whitespace before `extends`
        parse("interface Thing<T> extends A{ }") == ["interface", " ", "Thing", "<", "T", ">", " ", "extends", " ", "A" , "{", " ", "}"]
        parse("interface Thing <  T/* */> { }") == ["interface", " ", "Thing", " ", "<", "  ", "T", "/* */", ">", " ", "{", " ", "}"]
    }

    def "can parse optional package statement"() {
        expect:
        parse("package thing; class Thing { }") == ["package", " ", "thing", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("package a.b.c; class Thing { }") == ["package", " ", "a.b.c", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("  package   a.b.c   ; class Thing { }") == ["  ", "package", "   ", "a.b.c", "   ", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
    }

    def "can parse optional import statements"() {
        expect:
        parse("import thing; class Thing { }") == ["import", " ", "thing", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("import a.b.c; class Thing { }") == ["import", " ", "a.b.c", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("import a.b.c.*; class Thing { }") == ["import", " ", "a.b.c", ".", "*", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
        parse("import thing; import a.b; import a.b.c.*; class Thing { }") == ["import", " ", "thing", ";", " ", "import", " ", "a.b", ";", " ", "import", " ", "a.b.c", ".", "*", ";", " ", "class", " ", "Thing", " ", "{", " ", "}"]
    }

    def "can parse line comments"() {
        expect:
        parse("""
// comment class X { }
class Y{} // comment //
// comment""") == ["\n", "// comment class X { }\n", "class", " ", "Y", "{", "}", " ", "// comment //\n", "// comment"]

        parse("""
class // comment
Y// comment
{//comment
}//comment
""") == ["\n", "class", " ", "// comment\n", "Y", "// comment\n", "{", "//comment\n", "}", "//comment\n"]
    }

    def "can parse star comments"() {
        expect:
        parse("""
/*
    class X { } /* *
*/
class Y{} /* comment //
// comment*/""") == ["\n", "/*\n    class X { } /* *\n*/", "\n", "class", " ", "Y", "{", "}", " ", "/* comment //\n// comment*/"]

        parse("class/*  */Y/*  */extends/* */B/* */{/* */}") == ["class", "/*  */", "Y", "/*  */", "extends", "/* */", "B", "/* */", "{", "/* */", "}"]
    }

    def "can parse field declarations"() {
        expect:
        parse("class X{String x;}") == ["class", " ", "X", "{", "String", " ", "x", ";", "}"]
        parse("class X{final String abc;}") == ["class", " ", "X", "{", "final", " ", "String", " ", "abc", ";", "}"]
        parse("class X{private final String x;}") == ["class", " ", "X", "{", "private", " ", "final", " ", "String", " ", "x", ";", "}"]
        parse("class X{String x;String y;}") == ["class", " ", "X", "{", "String", " ", "x", ";", "String", " ", "y", ";", "}"]

        parse("""class X{
String abc;
final Long xyz;
int d;
}""") == ["class", " ", "X", "{", "\n", "String", " ", "abc", ";", "\n", "final", " ", "Long", " ", "xyz", ";", "\n", "int", " ", "d", ";", "\n", "}"]
        parse("""class X{String   /* */
   x
// ignore
   ;
   }""") == ["class", " ", "X", "{", "String", "   ", "/* */", "\n   ", "x", "\n", "// ignore\n", "   ", ";", "\n   ", "}"]
    }

    def "can parse class method declarations"() {
        expect:
        parse("class X{void x(){}}") == ["class", " ", "X", "{", "void", " ", "x", "(", ")", "{", "}", "}"]
        parse("class X{String x(){}}") == ["class", " ", "X", "{", "String", " ", "x", "(", ")", "{", "}", "}"]
        parse("class X{public static String x(){}}") == ["class", " ", "X", "{", "public", " ", "static", " ", "String", " ", "x", "(", ")", "{", "}", "}"]

        parse("class X{void x(){}String y(){}}") == ["class", " ", "X", "{", "void", " ", "x", "(", ")", "{", "}", "String", " ", "y", "(", ")", "{", "}", "}"]

        parse("""class X{
public  /* */static  String  x ( ) {
// comment
}
}""") == ["class", " ", "X", "{", "\n", "public", "  ", "/* */", "static", "  ", "String", "  ", "x", " ", "(", " ", ")", " ", "{", "\n", "// comment\n", "}", "\n", "}"]

        parse("class X{void x(String s){}}") == ["class", " ", "X", "{", "void", " ", "x", "(", "String", " ", "s", ")", "{", "}", "}"]
        parse("class X{void x(String abc,long y,int xyz){}}") == ["class", " ", "X", "{", "void", " ", "x", "(", "String", " ", "abc", ",", "long", " ", "y", ",", "int", " ", "xyz", ")", "{", "}", "}"]
        parse("class X{void x( String/* */abc  , long  y  , int xyz ){}}") == ["class", " ", "X", "{", "void", " ", "x", "(", " ", "String", "/* */", "abc", "  ", ",", " ", "long", "  ", "y", "  ", ",", " ", "int", " ", "xyz", " ", ")", "{", "}", "}"]
    }

    def "can parse interface method declarations"() {
        expect:
        parse("interface X{void x();}") == ["interface", " ", "X", "{", "void", " ", "x", "(", ")", ";", "}"]
        parse("interface X{String x();}") == ["interface", " ", "X", "{", "String", " ", "x", "(", ")", ";", "}"]
        parse("interface X{void x();String y();}") == ["interface", " ", "X", "{", "void", " ", "x", "(", ")", ";", "String", " ", "y", "(", ")", ";", "}"]

        parse("""interface X{
/* */String  x ( ) ;
// comment
}""") == ["interface", " ", "X", "{", "\n", "/* */", "String", "  ", "x", " ", "(", " ", ")", " ", ";", "\n", "// comment\n", "}"]

        parse("interface X{void x(int abc);}") == ["interface", " ", "X", "{", "void", " ", "x", "(", "int", " ", "abc", ")", ";", "}"]
        parse("interface X{void x(int abc,String c);}") == ["interface", " ", "X", "{", "void", " ", "x", "(", "int", " ", "abc", ",", "String", " ", "c", ")", ";", "}"]
    }

    def "can parse method annotations"() {
        expect:
        parse("interface X{@Override void x();}") == ["interface", " ", "X", "{", "@", "Override", " ", "void", " ", "x", "(", ")", ";", "}"]
        parse("interface X{@Override @Deprecated int x();}") == ["interface", " ", "X", "{", "@", "Override", " ", "@" , "Deprecated", " ", "int", " ", "x", "(", ")", ";", "}"]
        parse("interface X{@Override@Deprecated int x();}") == ["interface", " ", "X", "{", "@", "Override", "@", "Deprecated", " ", "int", " ", "x", "(", ")", ";", "}"]
        parse("interface X{  @  Override@/* */Deprecated int x();}") == ["interface", " ", "X", "{", "  ", "@", "  ", "Override", "@", "/* */", "Deprecated", " ", "int", " ", "x", "(", ")", ";", "}"]
    }

    def "can parse return statement"() {
        expect:
        parse("class X{X x(){return this;}}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", "return", " ", "this", ";", "}", "}"]
        parse("class X{X x(){  return/* */this  ;  }}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", "  ", "return", "/* */", "this", "  ", ";", "  ", "}", "}"]
    }

    def "can parse boolean literal expression"() {
        expect:
        parse("class X{boolean x(){return true;}}") == ["class", " ", "X", "{", "boolean", " ", "x", "(", ")", "{", "return", " ", "true", ";", "}", "}"]
        parse("class X{boolean x(){return false;}}") == ["class", " ", "X", "{", "boolean", " ", "x", "(", ")", "{", "return", " ", "false", ";", "}", "}"]
    }

    def "can parse new expression"() {
        expect:
        parse("class X{A x(){return new A();}}") == ["class", " ", "X", "{", "A", " ", "x", "(", ")", "{", "return", " ", "new", " ", "A", "(", ")", ";", "}", "}"]
        parse("class X{A x(){return new A(this);}}") == ["class", " ", "X", "{", "A", " ", "x", "(", ")", "{", "return", " ", "new", " ", "A", "(", "this", ")", ";", "}", "}"]
        parse("class X{A x(){return new A(false,true,this);}}") == ["class", " ", "X", "{", "A", " ", "x", "(", ")", "{", "return", " ", "new", " ", "A", "(", "false", ",", "true", ",", "this", ")", ";", "}", "}"]
        parse("class X{A x(){return   new A/* */(  false ,   this/* */)  ;}}") == ["class", " ", "X", "{", "A", " ", "x", "(", ")", "{", "return", "   ", "new", " ", "A", "/* */", "(", "  ", "false", " ", ",", "   ", "this", "/* */", ")", "  ", ";", "}", "}"]
    }

    def "stops parsing on first error"() {
        expect:
        def r0 = fail("")
        r0.tokens == []
        r0.failure == 'stopped at offset 0: end of input\nexpected: "\n", " ", "/*", "//", "abstract", "class", "import", "interface", "package", "public"'

        def r1 = fail(" Thing { }")
        r1.tokens == [" "]
        r1.failure == 'stopped at offset 1: [Thing { }]\nexpected: "\n", " ", "/*", "//", "abstract", "class", "import", "interface", "package", "public"'

        def r2 = fail("class x")
        r2.tokens == ["class", " ", "x"]
        r2.failure == 'stopped at offset 7: end of input\nexpected: "\n", " ", "/*", "//", "<", "{", {letter}'

        def r2_1 = fail("class x ")
        r2_1.tokens == ["class", " ", "x", " "]
        r2_1.failure == 'stopped at offset 8: end of input\nexpected: "\n", " ", "/*", "//", "<", "extends", "implements", "{"'

        def r2_2 = fail("class x{12")
        r2_2.tokens == ["class", " ", "x", "{"]
        r2_2.failure == 'stopped at offset 8: [12]\nexpected: "\n", " ", "/*", "//", "@", "final", "private", "public", "static", "void", "}", {letter}'

        def r3 = fail("class Thing extends { }")
        r3.tokens == ["class", " ", "Thing", " ", "extends", " "]
        r3.failure == 'stopped at offset 20: [{ }]\nexpected: "\n", " ", "/*", "//", {letter}'

        def r3_1 = fail("class Thing implements { }")
        r3_1.tokens == ["class", " ", "Thing", " ", "implements", " "]
        r3_1.failure == 'stopped at offset 23: [{ }]\nexpected: "\n", " ", "/*", "//", {letter}'

        def r4 = fail("class Thing implements A extends B { }")
        r4.tokens == ["class", " ", "Thing", " ", "implements", " ", "A", " "]
        r4.failure == 'stopped at offset 25: [extends B { }]\nexpected: "\n", " ", ",", "/*", "//", "{"'

        def r5 = fail("class Thing implements A implements B { }")
        r5.tokens == ["class", " ", "Thing", " ", "implements", " ", "A", " "]
        r5.failure == 'stopped at offset 25: [implements B { }]\nexpected: "\n", " ", ",", "/*", "//", "{"'

        def r6 = fail("class Thing extends A, B { }")
        r6.tokens == ["class", " ", "Thing", " ", "extends", " ", "A"]
        r6.failure == 'stopped at offset 21: [, B { }]\nexpected: "\n", " ", "/*", "//", "{", {letter}'

        // TODO - should really complain that interface can't be abstract
        // TODO - shouldn't offer abstract as an alternative
        def r7 = fail("abstract interface Thing extends { }")
        r7.tokens == ["abstract", " "]
        r7.failure == 'stopped at offset 9: [interface Thing exte]\nexpected: "abstract", "class", "public"'

        def r8 = fail("interface Thing implements A { }")
        r8.tokens == ["interface", " ", "Thing", " "]
        r8.failure == 'stopped at offset 16: [implements A { }]\nexpected: "\n", " ", "/*", "//", "<", "extends", "{"'

        def r9 = fail("x")
        r9.tokens == []
        r9.failure == 'stopped at offset 0: [x]\nexpected: "\n", " ", "/*", "//", "abstract", "class", "import", "interface", "package", "public"'

        // TODO - 'a.b' and '.' should be in same token
        def r10 = fail("package a.b.{")
        r10.tokens == ["package", " ", "a.b", "."]
        r10.failure == 'stopped at offset 12: [{]\nexpected: {letter}'

        def r11 = fail("package a.b import c.d")
        r11.tokens == ["package", " ", "a.b", " "]
        r11.failure == 'stopped at offset 12: [import c.d]\nexpected: "\n", " ", "/*", "//", ";"'

        def r12 = fail("package a.b; import a.b{}")
        r12.tokens == ["package", " ", "a.b", ";", " ", "import", " ", "a.b"]
        r12.failure == 'stopped at offset 23: [{}]\nexpected: "\n", " ", ".", "/*", "//", ";"'

        // TODO - 'a.b' and '.' should be same token
        // TODO missing '*' alternative
        def r13 = fail("package a.b; import a.b.%; class Thing { }")
        r13.tokens == ["package", " ", "a.b", ";", " ", "import", " ", "a.b", "."]
        r13.failure == 'stopped at offset 24: [%; class Thing { }]\nexpected: {letter}'

        // TODO - not quite right, should complain about an unexpected identifier
        def r14 = fail("packageimportclass")
        r14.tokens == ["package"]
        r14.failure == 'stopped at offset 7: [importclass]\nexpected: "\n", " ", "/*", "//"'

        // TODO - should complain about 'import' keyword instead of accepting it
        def r15 = fail("package import a;")
        r15.tokens == ["package", " ", "import", " "]
        r15.failure == 'stopped at offset 15: [a;]\nexpected: "\n", " ", "/*", "//", ";"'

        // TODO - missing '.' as an alternative
        def r16 = fail("package a")
        r16.tokens == ["package", " ", "a"]
        r16.failure == 'stopped at offset 9: end of input\nexpected: "\n", " ", "/*", "//", ";", {letter}'

        // TODO - missing {letter} as an alternative
        def r17 = fail("package a.b")
        r17.tokens == ["package", " ", "a.b"]
        r17.failure == 'stopped at offset 11: end of input\nexpected: "\n", " ", ".", "/*", "//", ";"'

        // TODO - missing '*' as an alternative
        def r18 = fail("import a.")
        r18.tokens == ["import", " ", "a", "."]
        r18.failure == 'stopped at offset 9: end of input\nexpected: {letter}'

        def r19 = fail("import a; ")
        r19.tokens == ["import", " ", "a", ";", " "]
        r19.failure == 'stopped at offset 10: end of input\nexpected: "abstract", "class", "import", "interface", "public"'

        // TODO - shouldn't offer 'public' as an alternative
        def r20 = fail("public ")
        r20.tokens == ["public", " "]
        r20.failure == 'stopped at offset 7: end of input\nexpected: "\n", " ", "/*", "//", "abstract", "class", "interface", "public"'

        // TODO - too many alternatives, should be '\n' only
        def r21 = fail("// abc")
        r21.tokens == ["// abc"]
        r21.failure == 'stopped at offset 6: end of input\nexpected: "\n", " ", "/*", "//", "abstract", "class", "import", "interface", "package", "public"'

        // TODO - expectation should be something like 'anything up to */'
        def r22 = fail("/* abc")
        r22.tokens == ["/* abc"]
        r22.failure == 'stopped at offset 6: end of input\nexpected: "*/", anything'

        def r23 = fail("class X { String; }")
        r23.tokens == ["class", " ", "X", " ", "{", " ", "String"]
        r23.failure == 'stopped at offset 16: [; }]\nexpected: "\n", " ", "/*", "//", {letter}'

        // TODO - missing tokens, should have stopped at end of input
        // TODO - too many alternatives, should be '*/' only
        def r24 = fail("class X { String x; /* }")
        r24.tokens == ["class", " ", "X", " ", "{", " ", "String", " ", "x", ";", " "]
        r24.failure == 'stopped at offset 20: [/* }]\nexpected: "@", "final", "private", "public", "static", "void", "}", {letter}'

        def r25 = fail("class X { String x(); }")
        r25.tokens == ["class", " ", "X", " ", "{", " ", "String", " ", "x", "(", ")"]
        r25.failure == 'stopped at offset 20: [; }]\nexpected: "\n", " ", "/*", "//", "{"'

        def r27 = fail("interface X { String x() { } }")
        r27.tokens == ["interface", " ", "X", " ", "{", " ", "String", " ", "x", "(", ")", " "]
        r27.failure == 'stopped at offset 25: [{ } }]\nexpected: "\n", " ", "/*", "//", ";"'

        // TODO - missing ',' and separator as alternatives
        def r28 = fail("interface X { String x(int a}")
        r28.tokens == ["interface", " ", "X", " ", "{", " ", "String", " ", "x", "(", "int", " ", "a"]
        r28.failure == 'stopped at offset 28: [}]\nexpected: ")", {letter}'

        // TODO - missing letter and separator as alternatives
        def r29 = fail("interface X { String x(int a,int b}")
        r29.tokens == ["interface", " ", "X", " ", "{", " ", "String", " ", "x", "(", "int", " ", "a", ",", "int", " ", "b"]
        r29.failure == 'stopped at offset 34: [}]\nexpected: ")", ","'

        def r30 = fail("interface X { @@ String x(int a,int b}")
        r30.tokens == ["interface", " ", "X", " ", "{", " ", "@"]
        r30.failure == 'stopped at offset 15: [@ String x(int a,int]\nexpected: "\n", " ", "/*", "//", {letter}'

        // TODO - missing whitespace as an alternative, shouldn't suggest void
        def r31 = fail("interface X { @a")
        r31.tokens == ["interface", " ", "X", " ", "{", " ", "@", "a"]
        r31.failure == 'stopped at offset 16: end of input\nexpected: "@", "void", {letter}'

        def r32 = fail("class X {String m(){return}")
        r32.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return"]
        r32.failure == 'stopped at offset 26: [}]\nexpected: "\n", " ", "/*", "//"'

        def r33 = fail("class X {String m(){return }")
        r33.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " "]
        r33.failure == 'stopped at offset 27: [}]\nexpected: "\n", " ", "/*", "//", "false", "new", "this", "true"'

        def r34 = fail("class X {String m(){return this}")
        r34.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "this"]
        r34.failure == 'stopped at offset 31: [}]\nexpected: "\n", " ", "/*", "//", ";"'

        def r35 = fail("class X {String m(){return new}")
        r35.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new"]
        r35.failure == 'stopped at offset 30: [}]\nexpected: "\n", " ", "/*", "//"'

        def r36 = fail("class X {String m(){return new 78}")
        r36.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new", " "]
        r36.failure == 'stopped at offset 31: [78}]\nexpected: "\n", " ", "/*", "//", {letter}'

        def r37 = fail("class X {String m(){return new A(}")
        r37.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new", " ", "A", "("]
        r37.failure == 'stopped at offset 33: [}]\nexpected: "\n", " ", ")", "/*", "//", {letter}'

        def r38 = fail("class X {String m(){return new A(a b c}")
        r38.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new", " ", "A", "(", "a", " "]
        r38.failure == 'stopped at offset 35: [b c}]\nexpected: "\n", " ", ")", ",", "/*", "//"'

        def r39 = fail("class X {String m(){return new A(a, }")
        r39.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new", " ", "A", "(", "a", ",", " "]
        r39.failure == 'stopped at offset 36: [}]\nexpected: "\n", " ", "/*", "//", {letter}'

        def r40 = fail("class X {String m(){return new A(a, b}")
        r40.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new", " ", "A", "(", "a", ",", " ", "b"]
        r40.failure == 'stopped at offset 37: [}]\nexpected: "\n", " ", ")", ",", "/*", "//"'
    }

    def List<String> parse(String str) {
        return parser.parse(str, new CollectingVisitor()).tokens
    }

    def CollectingVisitor fail(String str) {
        return parser.parse(str, new CollectingVisitor())
    }
}
