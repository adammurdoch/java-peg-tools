package net.rubygrapefruit.parser.peg

import net.rubygrapefruit.parser.peg.visitor.CollectingVisitor
import spock.lang.Specification

abstract class AbstractParserTest extends Specification {
    def builder = new ParserBuilder()

    def List<String> tokens(Parser parser, String str) {
        def visitor = parser.parse(str, new CollectingVisitor())
        assert visitor.failure == null
        return visitor.tokens
    }

    def CollectingVisitor parse(Parser parser, String str) {
        def visitor = parser.parse(str, new CollectingVisitor())
        assert visitor.failure == null
        return visitor
    }

    def CollectingVisitor fail(Parser parser, String str) {
        def visitor = parser.parse(str, new CollectingVisitor())
        assert visitor.failure != null
        return visitor
    }

}
