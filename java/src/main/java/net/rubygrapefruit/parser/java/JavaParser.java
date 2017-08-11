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

        Expression whitespace = builder.anyOf(' ', '\n');
        Expression whitespaceSeparator = builder.oneOrMore(whitespace).group();
        Expression optionalWhitespace = builder.zeroOrMore(whitespace).group();

        Expression classKeyword = builder.chars("class");
        Expression packageKeyword = builder.chars("package");

        Expression letters = builder.oneOrMore(builder.letter());
        Expression dot = builder.singleChar('.');
        Expression leftCurly = builder.singleChar('{');
        Expression rightCurly = builder.singleChar('}');
        Expression semiColon = builder.singleChar(';');

        Expression identifier = letters.group();
        Expression qualified = builder.sequence(letters, builder.zeroOrMore(builder.sequence(dot, letters))).group();

        Expression packageDeclaration = builder.sequence(packageKeyword, whitespaceSeparator, qualified, optionalWhitespace, semiColon);
        Expression optionalPackageDeclaration = builder.optional(packageDeclaration);

        Expression classDef = builder.sequence(optionalWhitespace, optionalPackageDeclaration, optionalWhitespace, classKeyword, whitespaceSeparator, identifier, optionalWhitespace, leftCurly, optionalWhitespace, rightCurly, optionalWhitespace);
        parser = builder.newParser(classDef);
    }

    /**
     * Parses the given Java source into a sequence of tokens.
     */
    public List<String> parse(String input) {
        return parser.parse(input);
    }
}
