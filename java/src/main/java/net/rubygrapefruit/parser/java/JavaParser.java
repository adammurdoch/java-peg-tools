package net.rubygrapefruit.parser.java;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.Parser;
import net.rubygrapefruit.parser.peg.ParserBuilder;
import net.rubygrapefruit.parser.peg.TokenVisitor;

/**
 * Parses Java source into a sequence of tokens.
 *
 * <p>Implementations are thread-safe.</p>
 */
public class JavaParser {
    private final Parser parser;

    public JavaParser() {
        ParserBuilder builder = new ParserBuilder();

        Expression eol = builder.singleChar('\n');

        Expression starComment = builder.sequence(builder.chars("/*"), builder.zeroOrMore(builder.sequence(builder.not(builder.chars("*/")), builder.anything())), builder.chars("*/")).group();
        Expression lineComment = builder.sequence(builder.chars("//"), builder.zeroOrMore(builder.sequence(builder.not(eol), builder.anything())), builder.optional(eol)).group();

        Expression whitespace = builder.oneOrMore(builder.oneOf(builder.singleChar(' '), eol)).group();
        Expression separator = builder.oneOf(whitespace, starComment, lineComment);
        Expression whitespaceSeparator = builder.oneOrMore(separator);
        Expression optionalWhitespace = builder.zeroOrMore(separator);

        Expression classKeyword = builder.chars("class");
        Expression interfaceKeyword = builder.chars("interface");
        Expression publicKeyword = builder.chars("public");
        Expression abstractKeyword = builder.chars("abstract");
        Expression packageKeyword = builder.chars("package");
        Expression importKeyword = builder.chars("import");
        Expression extendsKeyword = builder.chars("extends");
        Expression implementsKeyword = builder.chars("implements");

        Expression letters = builder.oneOrMore(builder.letter());
        Expression dot = builder.singleChar('.');
        Expression leftCurly = builder.singleChar('{');
        Expression rightCurly = builder.singleChar('}');
        Expression semiColon = builder.singleChar(';');
        Expression star = builder.singleChar('*');
        Expression comma = builder.singleChar(',');

        Expression identifier = letters.group();
        Expression qualified = builder.sequence(letters, builder.zeroOrMore(builder.sequence(dot, letters))).group();
        Expression starImport = builder.sequence(letters, builder.zeroOrMore(builder.sequence(dot, letters)), dot, star).group();

        Expression packageDeclaration = builder.sequence(packageKeyword, whitespaceSeparator, qualified, optionalWhitespace, semiColon, optionalWhitespace);
        Expression optionalPackageDeclaration = builder.optional(packageDeclaration);

        Expression importDeclaration = builder.sequence(importKeyword, whitespaceSeparator, builder.oneOf(starImport, qualified), optionalWhitespace, semiColon, optionalWhitespace);
        Expression importDeclarations = builder.zeroOrMore(importDeclaration);

        Expression identifierList = builder.sequence(identifier, builder.zeroOrMore(builder.sequence(optionalWhitespace, comma, optionalWhitespace, identifier)));
        Expression superClassDeclaration = builder.sequence(whitespaceSeparator, extendsKeyword, whitespaceSeparator, identifier);
        Expression superTypesDeclaration = builder.sequence(whitespaceSeparator, extendsKeyword, whitespaceSeparator, identifierList);
        Expression implementsDeclaration = builder.sequence(whitespaceSeparator, implementsKeyword, whitespaceSeparator, identifierList);

        Expression classModifiers = builder.zeroOrMore(builder.sequence(builder.oneOf(publicKeyword, abstractKeyword), whitespaceSeparator));
        Expression classDeclaration = builder.sequence(classModifiers, classKeyword, whitespaceSeparator, identifier, builder.optional(superClassDeclaration), builder.optional(implementsDeclaration), optionalWhitespace, leftCurly, optionalWhitespace, rightCurly);

        Expression interfaceModifiers = builder.optional(builder.sequence(publicKeyword, whitespaceSeparator));
        Expression interfaceDeclaration = builder.sequence(interfaceModifiers, interfaceKeyword, whitespaceSeparator, identifier, builder.optional(superTypesDeclaration), optionalWhitespace, leftCurly, optionalWhitespace, rightCurly);
        Expression typeDeclaration = builder.oneOf(classDeclaration, interfaceDeclaration);

        Expression classDef = builder.sequence(optionalWhitespace, optionalPackageDeclaration, importDeclarations, typeDeclaration, optionalWhitespace);
        parser = builder.newParser(classDef);
    }

    /**
     * Parses the given Java source into a sequence of tokens.
     */
    public <T extends TokenVisitor> T parse(String input, T visitor) {
        return parser.parse(input, visitor);
    }
}
