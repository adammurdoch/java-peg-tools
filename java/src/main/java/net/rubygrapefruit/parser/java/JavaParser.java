package net.rubygrapefruit.parser.java;

import net.rubygrapefruit.parser.peg.Parser;
import net.rubygrapefruit.parser.peg.ParserBuilder;
import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class JavaParser {
    private final Parser parser;

    public JavaParser() {
        ParserBuilder builder = new ParserBuilder();
        Expression classKeyword = builder.chars("class");
        Expression whitespace = builder.singleChar(' ');
        Expression word = builder.word();
        Expression leftCurly = builder.singleChar('{');
        Expression rightCurly = builder.singleChar('}');
        Expression classDef = builder.sequence(classKeyword, whitespace, word, whitespace, leftCurly, whitespace, rightCurly);
        parser = builder.newParser(classDef);
    }

    /**
     * Parses the given Java source.
     */
    public List<String> parse(String input) {
        return parser.parse(input);
    }
}
