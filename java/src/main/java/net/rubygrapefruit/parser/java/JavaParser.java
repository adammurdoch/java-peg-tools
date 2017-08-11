package net.rubygrapefruit.parser.java;

import net.rubygrapefruit.parser.peg.Parser;
import net.rubygrapefruit.parser.peg.ParserBuilder;
import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

/**
 * Parses Java source into a sequence of tokens.
 *
 * <p>Implementations are thread-safe.</p>
 */
public class JavaParser {
    private final Parser parser;

    public JavaParser() {
        ParserBuilder builder = new ParserBuilder();
        Expression classKeyword = builder.chars("class");
        Expression whitespace = builder.anyOf(' ', '\n');
        Expression separator = builder.oneOrMore(whitespace).group();
        Expression optionalWhitespace = builder.zeroOrMore(whitespace).group();
        Expression word = builder.oneOrMore(builder.letter()).group();
        Expression leftCurly = builder.singleChar('{');
        Expression rightCurly = builder.singleChar('}');
        Expression classDef = builder.sequence(optionalWhitespace, classKeyword, separator, word, optionalWhitespace, leftCurly, optionalWhitespace, rightCurly, optionalWhitespace);
        parser = builder.newParser(classDef);
    }

    /**
     * Parses the given Java source into a sequence of tokens.
     */
    public List<String> parse(String input) {
        return parser.parse(input);
    }
}
