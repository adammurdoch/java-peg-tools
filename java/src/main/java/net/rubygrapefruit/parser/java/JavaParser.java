package net.rubygrapefruit.parser.java;

import net.rubygrapefruit.parser.peg.*;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Parses Java source into a sequence of tokens.
 *
 * <p>Implementations are thread-safe.</p>
 */
public class JavaParser {
    private final Parser parser;
    private final Expression starComment;
    private final Expression lineComment;
    private final Expression whitespace;
    private final HashSet<Expression> keywords;

    public JavaParser() {
        ParserBuilder builder = new ParserBuilder();

        Expression eol = builder.singleChar('\n');

        starComment = builder.sequence(builder.chars("/*"), builder.zeroOrMore(builder.sequence(builder.not(builder.chars("*/")), builder.anything())), builder.chars("*/")).group();
        lineComment = builder.sequence(builder.chars("//"), builder.zeroOrMore(builder.sequence(builder.not(eol), builder.anything())), builder.optional(eol)).group();

        whitespace = builder.oneOrMore(builder.oneOf(builder.singleChar(' '), eol)).group();
        Expression separator = builder.oneOf(whitespace, starComment, lineComment);
        Expression whitespaceSeparator = builder.oneOrMore(separator);
        Expression optionalWhitespace = builder.zeroOrMore(separator);

        Expression classKeyword = builder.chars("class");
        Expression interfaceKeyword = builder.chars("interface");
        Expression privateKeyword = builder.chars("private");
        Expression finalKeyword = builder.chars("final");
        Expression publicKeyword = builder.chars("public");
        Expression abstractKeyword = builder.chars("abstract");
        Expression packageKeyword = builder.chars("package");
        Expression importKeyword = builder.chars("import");
        Expression extendsKeyword = builder.chars("extends");
        Expression implementsKeyword = builder.chars("implements");
        keywords = new HashSet<>(Arrays.asList(classKeyword, interfaceKeyword, privateKeyword, finalKeyword, publicKeyword, abstractKeyword, packageKeyword, importKeyword, extendsKeyword, implementsKeyword));

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

        Expression fieldModifiers = builder.zeroOrMore(builder.sequence(builder.oneOf(privateKeyword, finalKeyword), whitespaceSeparator));
        Expression fieldDeclaration = builder.sequence(fieldModifiers, identifier, whitespaceSeparator, identifier, optionalWhitespace, semiColon);
        Expression fieldDeclarations = builder.zeroOrMore(builder.sequence(fieldDeclaration, optionalWhitespace));
        Expression typeBody = builder.sequence(leftCurly, optionalWhitespace, fieldDeclarations, rightCurly);

        Expression classModifiers = builder.zeroOrMore(builder.sequence(builder.oneOf(publicKeyword, abstractKeyword), whitespaceSeparator));
        Expression classDeclaration = builder.sequence(classModifiers, classKeyword, whitespaceSeparator, identifier, builder.optional(superClassDeclaration), builder.optional(implementsDeclaration), optionalWhitespace, typeBody);

        Expression interfaceModifiers = builder.optional(builder.sequence(publicKeyword, whitespaceSeparator));
        Expression interfaceDeclaration = builder.sequence(interfaceModifiers, interfaceKeyword, whitespaceSeparator, identifier, builder.optional(superTypesDeclaration), optionalWhitespace, typeBody);
        Expression typeDeclaration = builder.oneOf(classDeclaration, interfaceDeclaration);

        Expression compilationUnit = builder.sequence(optionalWhitespace, optionalPackageDeclaration, importDeclarations, typeDeclaration, optionalWhitespace);
        parser = builder.newParser(compilationUnit);
    }

    /**
     * Parses the given Java source into a sequence of tokens.
     *
     * @param input The string to parse.
     * @param visitor The visitor to receive the results.
     * @return the visitor
     */
    public <T extends TokenVisitor<JavaToken>> T parse(String input, final T visitor) {
        parser.parse(input, new TokenVisitor<Expression>(){
            @Override
            public void token(Expression expression, Region match) {
                JavaToken token;
                if (expression == lineComment || expression == starComment) {
                    token = JavaToken.Comment;
                } else if (expression == whitespace) {
                    token = JavaToken.Whitespace;
                } else if (keywords.contains(expression)){
                    token = JavaToken.Keyword;
                } else {
                    token = JavaToken.Punctuation;
                }
                visitor.token(token, match);
            }

            @Override
            public void failed(String message, Region remainder) {
                visitor.failed(message, remainder);
            }
        });
        return visitor;
    }
}
