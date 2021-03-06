package net.rubygrapefruit.parser.peg;

import net.rubygrapefruit.parser.peg.internal.*;
import net.rubygrapefruit.parser.peg.internal.expression.*;
import net.rubygrapefruit.parser.peg.internal.match.MatchExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link Parser} from a root {@link Expression} specification.
 *
 * <p>Call {@link #newParser(Expression)} using the root expression, using the factory methods on this class to assemble the root expression.</p>
 */
public class ParserBuilder {
    /**
     * Matches the given sequence of characters, case sensitive.
     */
    public Expression chars(String str) {
        if (str.isEmpty()) {
            throw new IllegalArgumentException("At least one character required.");
        }
        return new CharSequenceExpression(str);
    }

    /**
     * Matches the given character, case sensitive.
     */
    public Expression singleChar(char ch) {
        return new CharSequenceExpression(Character.toString(ch));
    }

    /**
     * Matches a single letter, as per {@link Character#isAlphabetic(int)}.
     */
    public Expression letter() {
        return new LetterExpression();
    }

    /**
     * Matches any single character.
     */
    public Expression anything() {
        return new AnythingExpression();
    }

    /**
     * Matches one of the given expressions. Order is significant and the first matching expression is selected, regardless of whether later expressions might also match.
     */
    public Expression oneOf(Expression... expressions) {
        if (expressions.length < 2) {
            throw new IllegalArgumentException("At least two expressions required.");
        }
        return new OneOfExpression(matchers(expressions));
    }

    /**
     * Matches zero or one of the given expression.
     */
    public Expression optional(Expression expression) {
        return new OptionalExpression(matcher(expression));
    }

    /**
     * Matches one or more of the given expressions. Matching is greedy.
     */
    public Expression oneOrMore(Expression expression) {
        MatchExpression matchExpression = matcher(expression);
        return new OneOrMoreExpression(matchExpression);
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
     * Returns an expression that matches anything that does not match the given expression, but does not consume anything.
     */
    public Expression not(Expression expression) {
        return new NotPredicate(matcher(expression));
    }

    /**
     * Creates an expression that references another expression. This can be used to create recursive expressions.
     */
    public ReferenceExpression reference() {
        return new DefaultReferenceExpression();
    }

    /**
     * Creates a new back reference to the result of the given expression.
     */
    public BackReference backReference(Expression expression) {
        return new DefaultBackReference(matcher(expression));
    }

    /**
     * Creates a parser for the given expression.
     */
    public Parser newParser(Expression expression) {
        return new DefaultParser(matcher(expression));
    }

    private List<MatchExpression> matchers(Expression... expressions) {
        List<MatchExpression> matchers = new ArrayList<>(expressions.length);
        for (Expression expression : expressions) {
            matchers.add(matcher(expression));
        }
        return matchers;
    }

    private MatchExpression matcher(Expression expression) {
        return (MatchExpression) expression;
    }
}
