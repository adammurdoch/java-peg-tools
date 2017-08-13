package net.rubygrapefruit.parser.peg.internal;

import java.util.List;

public interface MatchExpression {
    /**
     * Returns the matcher for this expression.
     */
    Matcher getMatcher();

    /**
     * Notifies the given visitor of the result of matching this expression.
     */
    void collectResult(List<String> tokens, MatchVisitor visitor);
}
