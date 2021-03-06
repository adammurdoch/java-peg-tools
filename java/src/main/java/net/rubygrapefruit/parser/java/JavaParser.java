package net.rubygrapefruit.parser.java;

import net.rubygrapefruit.parser.peg.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    private final Set<Expression> keywords;
    private final Set<Expression> identifiers;

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
        Expression protectedKeyword = builder.chars("protected");
        Expression finalKeyword = builder.chars("final");
        Expression publicKeyword = builder.chars("public");
        Expression abstractKeyword = builder.chars("abstract");
        Expression packageKeyword = builder.chars("package");
        Expression importKeyword = builder.chars("import");
        Expression extendsKeyword = builder.chars("extends");
        Expression implementsKeyword = builder.chars("implements");
        Expression voidKeyword = builder.chars("void");
        Expression staticKeyword = builder.chars("static");
        Expression returnKeyword = builder.chars("return");
        Expression thisKeyword = builder.chars("this");
        Expression newKeyword = builder.chars("new");
        keywords = new HashSet<>(Arrays.asList(classKeyword, interfaceKeyword, privateKeyword, protectedKeyword, finalKeyword, publicKeyword, abstractKeyword, packageKeyword, importKeyword, extendsKeyword, implementsKeyword, voidKeyword, staticKeyword, returnKeyword, thisKeyword, newKeyword));

        Expression letters = builder.oneOrMore(builder.letter());
        Expression dot = builder.singleChar('.');
        Expression leftCurly = builder.singleChar('{');
        Expression rightCurly = builder.singleChar('}');
        Expression semiColon = builder.singleChar(';');
        Expression star = builder.singleChar('*');
        Expression comma = builder.singleChar(',');
        Expression leftParen = builder.singleChar('(');
        Expression rightParen = builder.singleChar(')');
        Expression leftAngle = builder.singleChar('<');
        Expression rightAngle = builder.singleChar('>');
        Expression at = builder.singleChar('@');
        Expression equal = builder.singleChar('=');

        Expression literalTrue = builder.chars("true");
        Expression literalFalse = builder.chars("false");

        Expression identifier = letters.group();
        Expression qualified = builder.sequence(letters, builder.zeroOrMore(builder.sequence(dot, letters))).group();
        BackReference className = builder.backReference(identifier);
        identifiers = new HashSet<>(Arrays.asList(identifier, qualified, className.getValue()));

        Expression starImport = builder.sequence(qualified, dot, star);

        Expression packageDeclaration = builder.sequence(packageKeyword, whitespaceSeparator, qualified, optionalWhitespace, semiColon, optionalWhitespace);
        Expression optionalPackageDeclaration = builder.optional(packageDeclaration);

        Expression importDeclaration = builder.sequence(importKeyword, whitespaceSeparator, builder.oneOf(starImport, qualified), optionalWhitespace, semiColon, optionalWhitespace);
        Expression importDeclarations = builder.zeroOrMore(importDeclaration);

        Expression identifierList = builder.sequence(
                identifier, builder.zeroOrMore(builder.sequence(optionalWhitespace, comma, optionalWhitespace, identifier)));
        Expression superClassDeclaration = builder.sequence(whitespaceSeparator, extendsKeyword, whitespaceSeparator, identifier);
        Expression superTypesDeclaration = builder.sequence(whitespaceSeparator, extendsKeyword, whitespaceSeparator, identifierList);
        Expression implementsDeclaration = builder.sequence(whitespaceSeparator, implementsKeyword, whitespaceSeparator, identifierList);

        Expression fieldModifiers = builder.zeroOrMore(builder.sequence(builder.oneOf(privateKeyword, finalKeyword), whitespaceSeparator));
        Expression fieldDeclaration = builder.sequence(fieldModifiers, identifier, whitespaceSeparator, identifier, optionalWhitespace, semiColon);

        Expression methodArgs = builder.optional(builder.sequence(identifier, optionalWhitespace,
                builder.zeroOrMore(builder.sequence(comma, optionalWhitespace, identifier)), optionalWhitespace));

        Expression newExpression = builder.sequence(newKeyword, whitespaceSeparator, identifier, optionalWhitespace, leftParen, optionalWhitespace, methodArgs, optionalWhitespace, rightParen);

        Expression fieldReference = builder.oneOf(builder.sequence(thisKeyword, optionalWhitespace, dot, optionalWhitespace, identifier), identifier);

        Expression expression = builder.oneOf(literalTrue, literalFalse, newExpression, fieldReference, thisKeyword);

        Expression returnStatement = builder.sequence(returnKeyword, whitespaceSeparator, expression, optionalWhitespace, semiColon);
        Expression assignmentStatement = builder.sequence(fieldReference, optionalWhitespace, equal, optionalWhitespace, expression, optionalWhitespace, semiColon);
        Expression statement = builder.oneOf(returnStatement, assignmentStatement);
        Expression statements = builder.zeroOrMore(builder.sequence(statement, optionalWhitespace));

        Expression annotation = builder.sequence(at, optionalWhitespace, identifier);
        Expression annotations = builder.zeroOrMore(builder.sequence(annotation, optionalWhitespace));

        Expression methodParams = builder.optional(builder.sequence(identifier, whitespaceSeparator, identifier, optionalWhitespace, builder.zeroOrMore(builder.sequence(comma, optionalWhitespace, identifier, whitespaceSeparator, identifier, optionalWhitespace))));

        Expression classMethodModifiers = builder.zeroOrMore(builder.sequence(builder.oneOf(publicKeyword, staticKeyword), whitespaceSeparator));
        Expression methodSignature = builder.sequence(builder.oneOf(voidKeyword, identifier), whitespaceSeparator, identifier, optionalWhitespace, leftParen, optionalWhitespace, methodParams, rightParen);
        Expression methodBody = builder.sequence(leftCurly, optionalWhitespace, statements, rightCurly);
        Expression classMethodDeclaration = builder.sequence(annotations, classMethodModifiers, methodSignature, optionalWhitespace, methodBody);
        Expression interfaceMethodDeclaration = builder.sequence(annotations, methodSignature, optionalWhitespace, semiColon);

        Expression constructorModifiers = builder.zeroOrMore(builder.sequence(builder.oneOf(publicKeyword, privateKeyword, protectedKeyword), whitespaceSeparator));
        Expression classConstructor = builder.sequence(constructorModifiers, className.getValue(), optionalWhitespace, leftParen, optionalWhitespace, methodParams, rightParen, optionalWhitespace, methodBody);

        Expression classMembers = builder.zeroOrMore(builder.sequence(builder.oneOf(fieldDeclaration, classConstructor, classMethodDeclaration), optionalWhitespace));
        Expression interfaceMembers = builder.zeroOrMore(builder.sequence(interfaceMethodDeclaration, optionalWhitespace));

        Expression classBody = builder.sequence(leftCurly, optionalWhitespace, classMembers, rightCurly);
        Expression interfaceBody = builder.sequence(leftCurly, optionalWhitespace, interfaceMembers, rightCurly);

        Expression typeParam = builder.sequence(optionalWhitespace, leftAngle, optionalWhitespace, identifier, optionalWhitespace, rightAngle);

        Expression classModifiers = builder.zeroOrMore(builder.sequence(builder.oneOf(publicKeyword, abstractKeyword), whitespaceSeparator));
        Expression classDeclaration = builder.sequence(classModifiers, classKeyword, whitespaceSeparator, className.followedBy(builder.sequence(builder.optional(typeParam), builder.optional(superClassDeclaration), builder.optional(implementsDeclaration), optionalWhitespace, classBody)));

        Expression interfaceModifiers = builder.optional(builder.sequence(publicKeyword, whitespaceSeparator));
        Expression interfaceDeclaration = builder.sequence(interfaceModifiers, interfaceKeyword, whitespaceSeparator, identifier, builder.optional(typeParam), builder.optional(superTypesDeclaration), optionalWhitespace, interfaceBody);
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
                } else if (identifiers.contains(expression)){
                    token = JavaToken.Identifier;
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
