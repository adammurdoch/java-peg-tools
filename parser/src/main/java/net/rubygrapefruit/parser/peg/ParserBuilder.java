package net.rubygrapefruit.parser.peg;

import net.rubygrapefruit.parser.peg.internal.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link Parser} from a root {@link Expression} specification.
 */
public class ParserBuilder {
    /**
     * Matches the given sequence of characters, case sensitive.
     */
    public Expression chars(String str) {
        return new CharSequenceExpression(str);
    }

    /**
     * Matches a single character.
     */
    public Expression singleChar(char ch) {
        return new SingleCharExpression(ch);
    }

    /**
     * Matches a single character, from the given chars.
     */
    public Expression anyOf(char... chars) {
        List<Matcher> matchers = new ArrayList<Matcher>(chars.length);
        for (char ch : chars) {
            matchers.add(new SingleCharExpression(ch));
        }
        return new AnyOfExpression(matchers);
    }

    /**
     * Matches a single letter.
     */
    public Expression letter() {
        return new LetterExpression();
    }

    /**
     * Matches one or more of the given expressions. Matching is greedy.
     */
    public Expression oneOrMore(Expression expression) {
        return new OneOrMoreExpression(matcher(expression));
    }

    /**
     * Matches zero or more of the given expressions. Matching is greedy.
     */
    public Expression zeroOrMore(Expression expression) {
        return new ZeroOrMoreExpression(matcher(expression));
    }

    /**
     * Matches the given sequence of expressions.
     */
    public Expression sequence(Expression... expressions) {
        return new SequenceExpression(matchers(expressions));
    }

    /**
     * Creates a parser for the given expression.
     */
    public Parser newParser(Expression expression) {
        return new DefaultParser(matcher(expression));
    }

    private List<Matcher> matchers(Expression... expressions) {
        List<Matcher> matchers = new ArrayList<>(expressions.length);
        for (Expression expression : expressions) {
            matchers.add(matcher(expression));
        }
        return matchers;
    }

    private Matcher matcher(Expression expression) {
        return (Matcher) expression;
    }
}
