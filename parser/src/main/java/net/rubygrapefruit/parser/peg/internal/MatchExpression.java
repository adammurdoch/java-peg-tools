package net.rubygrapefruit.parser.peg.internal;

public interface MatchExpression {
    /**
     * Returns the matcher for this expression.
     */
    Matcher getMatcher();

    /**
     * Creates a visitor to receive the results of matching this expression and that transforms and forwards the results to the given visitor.
     */
    ResultCollector collector(TokenCollector collector);
}
