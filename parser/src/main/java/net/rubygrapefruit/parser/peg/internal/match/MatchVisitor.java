package net.rubygrapefruit.parser.peg.internal.match;

import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

public interface MatchVisitor {
    /**
     * Called to indicate that the given expression was attempted but was not accepted.
     */
    void attempted(ExpressionMatchResult result);

    /**
     * Called to indicate that the given atomic expression was attempted but did not match anything.
     */
    void attempted(StreamPos pos, MatchPoint nextExpression);

    /**
     * Called to indicate an expression matched but consumed no input.
     */
    void matched(StreamPos pos);

    /**
     * Called to indicate that the given atomic expression was attempted and the result accepted.
     */
    void matched(MatchResult result);

    /**
     * Called to indicate that the given composite expression was attempted and the result accepted.
     */
    void matched(ExpressionMatchResult result);
}
