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

    def "can parse class constructor declarations"() {
        expect:
        parse("class X{X(){}}") == ["class", " ", "X", "{", "X", "(", ")", "{", "}", "}"]
        parse("class X{X(int x){}}") == ["class", " ", "X", "{", "X", "(", "int", " ", "x", ")", "{", "}", "}"]
        parse("class X{X(int x,String y){}}") == ["class", " ", "X", "{", "X", "(", "int", " ", "x", ",", "String", " ", "y", ")", "{", "}", "}"]
        parse("class X{  X  ( int   x  )  { }  }") == ["class", " ", "X", "{", "  ", "X", "  ", "(", " ", "int", "   ",  "x", "  ", ")", "  ", "{", " ", "}", "  ", "}"]

        parse("class X{public X(){}}") == ["class", " ", "X", "{", "public", " ", "X", "(", ")", "{", "}", "}"]
        parse("class X{private X(){}}") == ["class", " ", "X", "{", "private", " ", "X", "(", ")", "{", "}", "}"]
        parse("class X{protected X(){}}") == ["class", " ", "X", "{", "protected", " ", "X", "(", ")", "{", "}", "}"]
        parse("class X{/* */protected   X(){}}") == ["class", " ", "X", "{", "/* */", "protected", "   ", "X", "(", ")", "{", "}", "}"]

        parse("class X{X(){a=b;}}") == ["class", " ", "X", "{", "X", "(", ")", "{", "a", "=", "b", ";", "}", "}"]
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

    def "can parse assignment statement"() {
        expect:
        parse("class X{X x(){a=b;}}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", "a", "=", "b", ";", "}", "}"]
        parse("class X{X x(){this.a=b;}}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", "this", ".", "a", "=", "b", ";", "}", "}"]
        parse("class X{X x(){a=true;}}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", "a", "=", "true", ";", "}", "}"]
        parse("class X{X x(){a=null;}}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", "a", "=", "null", ";", "}", "}"]
        parse("class X{X x(){a=this;}}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", "a", "=", "this", ";", "}", "}"]
        parse("class X{X x(){a=this.b;}}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", "a", "=", "this", ".", "b", ";", "}", "}"]
        parse("class X{X x(){a=new X(abc);}}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", "a", "=", "new", " ", "X", "(", "abc", ")", ";", "}", "}"]

        parse("class X{X x(){ a  =/* */b  ; }}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", " ", "a", "  ", "=", "/* */", "b", "  ", ";", " ", "}", "}"]
    }

    def "can parse field reference expression"() {
        expect:
        parse("class X{X x(){ this  ./* */a  =/* */this . b  ; }}") == ["class", " ", "X", "{", "X", " ", "x", "(", ")", "{", " ", "this", "  ", ".", "/* */", "a", "  ", "=", "/* */", "this", " ", ".", " ", "b", "  ", ";", " ", "}", "}"]
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
        r0.failure == '''line 1: expected " ", "/*", "//", "\\n", "abstract", "class", "import", "interface", "package" or "public"

^'''

        def r1 = fail(" Thing { }")
        r1.tokens == [" "]
        r1.failure == '''line 1: expected " ", "/*", "//", "\\n", "abstract", "class", "import", "interface", "package" or "public"
 Thing { }
 ^'''

        def r2 = fail("class x")
        r2.tokens == ["class", " ", "x"]
        r2.failure == '''line 1: expected " ", "/*", "//", "<", "\\n", "{" or letter
class x
       ^'''

        def r2_1 = fail("class x ")
        r2_1.tokens == ["class", " ", "x", " "]
        r2_1.failure == '''line 1: expected " ", "/*", "//", "<", "\\n", "extends", "implements" or "{"
class x 
        ^'''

        def r2_2 = fail("class x{12")
        r2_2.tokens == ["class", " ", "x", "{"]
        r2_2.failure == '''line 1: expected " ", "/*", "//", "@", "\\n", "final", "private", "protected", "public", "static", "void", "x", "}" or letter
class x{12
        ^'''

        def r3 = fail("class Thing extends { }")
        r3.tokens == ["class", " ", "Thing", " ", "extends", " "]
        r3.failure == '''line 1: expected " ", "/*", "//", "\\n" or letter
class Thing extends { }
                    ^'''

        def r3_1 = fail("class Thing implements { }")
        r3_1.tokens == ["class", " ", "Thing", " ", "implements", " "]
        r3_1.failure == '''line 1: expected " ", "/*", "//", "\\n" or letter
class Thing implements { }
                       ^'''

        def r4 = fail("class Thing implements A extends B { }")
        r4.tokens == ["class", " ", "Thing", " ", "implements", " ", "A", " "]
        r4.failure == '''line 1: expected " ", ",", "/*", "//", "\\n" or "{"
class Thing implements A extends B { }
                         ^'''

        def r5 = fail("class Thing implements A implements B { }")
        r5.tokens == ["class", " ", "Thing", " ", "implements", " ", "A", " "]
        r5.failure == '''line 1: expected " ", ",", "/*", "//", "\\n" or "{"
class Thing implements A implements B { }
                         ^'''

        def r6 = fail("class Thing extends A, B { }")
        r6.tokens == ["class", " ", "Thing", " ", "extends", " ", "A"]
        r6.failure == '''line 1: expected " ", "/*", "//", "\\n", "{" or letter
class Thing extends A, B { }
                     ^'''

        // TODO - should really complain that interface can't be abstract
        // TODO - shouldn't offer abstract as an alternative
        def r7 = fail("abstract interface Thing extends { }")
        r7.tokens == ["abstract", " "]
        r7.failure == '''line 1: expected " ", "/*", "//", "\\n", "abstract", "class" or "public"
abstract interface Thing extends { }
         ^'''

        def r8 = fail("interface Thing implements A { }")
        r8.tokens == ["interface", " ", "Thing", " "]
        r8.failure == '''line 1: expected " ", "/*", "//", "<", "\\n", "extends" or "{"
interface Thing implements A { }
                ^'''

        def r9 = fail("x")
        r9.tokens == []
        r9.failure == '''line 1: expected " ", "/*", "//", "\\n", "abstract", "class", "import", "interface", "package" or "public"
x
^'''

        // TODO - 'a.b' and '.' should be in same token
        def r10 = fail("package a.b.{")
        r10.tokens == ["package", " ", "a.b", "."]
        r10.failure == '''line 1: expected letter
package a.b.{
            ^'''

        def r11 = fail("package a.b import c.d")
        r11.tokens == ["package", " ", "a.b", " "]
        r11.failure == '''line 1: expected " ", "/*", "//", ";" or "\\n"
package a.b import c.d
            ^'''

        def r12 = fail("package a.b; import a.b{}")
        r12.tokens == ["package", " ", "a.b", ";", " ", "import", " ", "a.b"]
        r12.failure == '''line 1: expected " ", ".", "/*", "//", ";", "\\n" or letter
package a.b; import a.b{}
                       ^'''

        // TODO - 'a.b' and '.' should be same token
        // TODO missing whitespace alternatives
        def r13 = fail("package a.b; import a.b.%;\nclass Thing { }")
        r13.tokens == ["package", " ", "a.b", ";", " ", "import", " ", "a.b", "."]
        r13.failure == '''line 1: expected "*" or letter
package a.b; import a.b.%;
                        ^'''

        // TODO - should complain about an unexpected identifier
        def r14 = fail("\n\npackageimportclass")
        r14.tokens == ["\n\n", "package"]
        r14.failure == '''line 3: expected " ", "/*", "//" or "\\n"
packageimportclass
       ^'''

        // TODO - should complain about 'import' keyword (or missing identifier before 'import') instead of accepting it
        def r15 = fail("package import a;")
        r15.tokens == ["package", " ", "import", " "]
        r15.failure == '''line 1: expected " ", "/*", "//", ";" or "\\n"
package import a;
               ^'''

        def r16 = fail("package a")
        r16.tokens == ["package", " ", "a"]
        r16.failure == '''line 1: expected " ", ".", "/*", "//", ";", "\\n" or letter
package a
         ^'''

        // TODO - should allow whitespace after '.'
        def r16a = fail("package a.")
        r16a.tokens == ["package", " ", "a", "."]
        r16a.failure == '''line 1: expected letter
package a.
          ^'''

        def r17 = fail("package a.b")
        r17.tokens == ["package", " ", "a.b"]
        r17.failure == '''line 1: expected " ", ".", "/*", "//", ";", "\\n" or letter
package a.b
           ^'''

        // TODO - missing whitespace alternatives
        def r18 = fail("import a.")
        r18.tokens == ["import", " ", "a", "."]
        r18.failure == '''line 1: expected "*" or letter
import a.
         ^'''

        def r18a = fail("import a")
        r18a.tokens == ["import", " ", "a"]
        r18a.failure == '''line 1: expected " ", ".", "/*", "//", ";", "\\n" or letter
import a
        ^'''

        def r19 = fail("import a; ")
        r19.tokens == ["import", " ", "a", ";", " "]
        r19.failure == '''line 1: expected " ", "/*", "//", "\\n", "abstract", "class", "import", "interface" or "public"
import a; 
          ^'''

        // TODO - shouldn't offer 'public' as an alternative
        def r20 = fail("public ")
        r20.tokens == ["public", " "]
        r20.failure == '''line 1: expected " ", "/*", "//", "\\n", "abstract", "class", "interface" or "public"
public 
       ^'''

        // TODO - too many alternatives, should be '\n' only
        def r21 = fail("// abc")
        r21.tokens == ["// abc"]
        r21.failure == '''line 1: expected " ", "/*", "//", "\\n", "abstract", "class", "import", "interface", "package", "public" or anything
// abc
      ^'''

        // TODO - expectation should be something like 'anything up to */'
        def r22 = fail("/* abc")
        r22.tokens == ["/* abc"]
        r22.failure == '''line 1: expected "*/" or anything
/* abc
      ^'''

        def r23 = fail("class X { String; }")
        r23.tokens == ["class", " ", "X", " ", "{", " ", "String"]
        r23.failure == '''line 1: expected " ", "/*", "//", "\\n" or letter
class X { String; }
                ^'''

        // TODO - improve expectation
        def r24 = fail("class X { String x; /* }")
        r24.tokens == ["class", " ", "X", " ", "{", " ", "String", " ", "x", ";", " ", "/* }"]
        r24.failure == '''line 1: expected "*/" or anything
class X { String x; /* }
                        ^'''

        def r25 = fail("class X { String x(); }")
        r25.tokens == ["class", " ", "X", " ", "{", " ", "String", " ", "x", "(", ")"]
        r25.failure == '''line 1: expected " ", "/*", "//", "\\n" or "{"
class X { String x(); }
                    ^'''

        def r27 = fail("interface X { String x() { } }")
        r27.tokens == ["interface", " ", "X", " ", "{", " ", "String", " ", "x", "(", ")", " "]
        r27.failure == '''line 1: expected " ", "/*", "//", ";" or "\\n"
interface X { String x() { } }
                         ^'''

        def r28 = fail("interface X { String x(int a}")
        r28.tokens == ["interface", " ", "X", " ", "{", " ", "String", " ", "x", "(", "int", " ", "a"]
        r28.failure == '''line 1: expected " ", ")", ",", "/*", "//", "\\n" or letter
interface X { String x(int a}
                            ^'''

        def r29 = fail("interface X { String x(int a,int b}")
        r29.tokens == ["interface", " ", "X", " ", "{", " ", "String", " ", "x", "(", "int", " ", "a", ",", "int", " ", "b"]
        r29.failure == '''line 1: expected " ", ")", ",", "/*", "//", "\\n" or letter
interface X { String x(int a,int b}
                                  ^'''

        def r29_1 = fail("class X { X }")
        r29_1.tokens == ["class", " ", "X", " ", "{", " ", "X", " "]
        r29_1.failure == '''line 1: expected " ", "(", "/*", "//", "\\n" or letter
class X { X }
            ^'''

        def r30 = fail("interface X { @@ String x(int a,int b}")
        r30.tokens == ["interface", " ", "X", " ", "{", " ", "@"]
        r30.failure == '''line 1: expected " ", "/*", "//", "\\n" or letter
interface X { @@ String x(int a,int b}
               ^'''

        // TODO - shouldn't suggest void
        def r31 = fail("interface X { @a")
        r31.tokens == ["interface", " ", "X", " ", "{", " ", "@", "a"]
        r31.failure == '''line 1: expected " ", "/*", "//", "@", "\\n", "void" or letter
interface X { @a
                ^'''

        // TODO - shouldn't suggest '='
        def r32 = fail("class X {String m(){return}")
        r32.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return"]
        r32.failure == '''line 1: expected " ", "/*", "//", "=", "\\n" or letter
class X {String m(){return}
                          ^'''

        // TODO shouldn't suggest '='
        def r33 = fail("class X {String m(){return }")
        r33.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " "]
        r33.failure == '''line 1: expected " ", "/*", "//", "=", "\\n", "false", "new", "this", "true" or letter
class X {String m(){return }
                           ^'''

        def r34 = fail("class X {String m(){return this}")
        r34.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "this"]
        r34.failure == '''line 1: expected " ", ".", "/*", "//", ";", "\\n" or letter
class X {String m(){return this}
                               ^'''

        // TODO - shouldn't suggest ';'
        def r35 = fail("class X {String m(){return new}")
        r35.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new"]
        r35.failure == '''line 1: expected " ", "/*", "//", ";", "\\n" or letter
class X {String m(){return new}
                              ^'''

        // TODO should suggest letter, should not suggest ';'
        def r36 = fail("class X {String m(){return new 78}")
        r36.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new", " "]
        r36.failure == '''line 1: expected " ", "/*", "//", ";" or "\\n"
class X {String m(){return new 78}
                               ^'''

        def r37 = fail("class X {String m(){return new A(}")
        r37.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new", " ", "A", "("]
        r37.failure == '''line 1: expected " ", ")", "/*", "//", "\\n" or letter
class X {String m(){return new A(}
                                 ^'''

        def r38 = fail("class X {String m(){return new A(a b c}")
        r38.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new", " ", "A", "(", "a", " "]
        r38.failure == '''line 1: expected " ", ")", ",", "/*", "//" or "\\n"
class X {String m(){return new A(a b c}
                                   ^'''

        def r39 = fail("class X {String m(){return new A(a, }")
        r39.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new", " ", "A", "(", "a", ",", " "]
        r39.failure == '''line 1: expected " ", "/*", "//", "\\n" or letter
class X {String m(){return new A(a, }
                                    ^'''

        def r40 = fail("class X {String m(){return new A(a, b}")
        r40.tokens == ["class", " ", "X", " ", "{", "String", " ", "m", "(", ")", "{", "return", " ", "new", " ", "A", "(", "a", ",", " ", "b"]
        r40.failure == '''line 1: expected " ", ")", ",", "/*", "//" or "\\n"
class X {String m(){return new A(a, b}
                                     ^'''

        // TODO - tests for field references and assignment statement
        false
    }

    def List<String> parse(String str) {
        return parser.parse(str, new CollectingVisitor()).tokens
    }

    def CollectingVisitor fail(String str) {
        return parser.parse(str, new CollectingVisitor())
    }
}
