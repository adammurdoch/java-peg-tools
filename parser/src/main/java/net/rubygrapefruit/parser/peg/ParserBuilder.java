package net.rubygrapefruit.parser.peg;

import net.rubygrapefruit.parser.peg.internal.*;

import java.util.ArrayList;
import java.util.Arrays;
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
     * Matches the given character, case sensitive.
     */
    public Expression singleChar(char ch) {
        return new CharSequenceExpression(Character.toString(ch));
    }

    /**
     * Matches a single letter.
     */
    public Expression letter() {
        return new LetterExpression();
    }

    /**
     * Matches one of the given expressions. Order is significant and the first matching expression is selected.
     */
    public Expression oneOf(Expression... expressions) {
        return new OneOfExpression(matchers(expressions));
    }

    /**
     * Matches zero or one of the given expressions.
     */
    public Expression optional(Expression expression) {
        return new OptionalExpression(matcher(expression));
    }

    /**
     * Matches one or more of the given expressions. Matching is greedy.
     */
    public Expression oneOrMore(Expression expression) {
        Matcher matcher = matcher(expression);
        return new SequenceExpression(Arrays.asList(matcher, new ZeroOrMoreExpression(matcher)));
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
        if (expressions.length < 2) {
            throw new IllegalArgumentException("At least two expressions required.");
        }
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
