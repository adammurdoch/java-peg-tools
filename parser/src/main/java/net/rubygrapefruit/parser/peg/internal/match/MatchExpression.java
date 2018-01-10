package net.rubygrapefruit.parser.peg.internal.match;

public interface MatchExpression {
    /**
     * Returns the matcher for this expression.
     */
    Matcher getMatcher();
}
