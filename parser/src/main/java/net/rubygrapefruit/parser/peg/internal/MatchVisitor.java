package net.rubygrapefruit.parser.peg.internal;

public interface MatchVisitor extends TokenCollector {
    /**
     * Records a match. All calls made up to the last {@link #matched(CharStream)} call are considered part of the result. All calls made up to {@link #stoppedAt(CharStream, MatchPoint)} are considered potential candidates.
     */
    @Override
    void token(MatchResult token);

    /**
     * Accepts all input up to the given position. May be called zero or more times.
     */
    void matched(CharStream endPos);

    /**
     * Indicates where matching stopped.
     *
     * @param nextExpression Can be null.
     */
    void stoppedAt(CharStream stoppedAt, MatchPoint nextExpression);
}
