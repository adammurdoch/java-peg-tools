package net.rubygrapefruit.parser.peg.internal;

public interface MatchVisitor extends TokenCollector {
    /**
     * Records a match. All calls made up to the last {@link #matched(StreamPos)} call are considered part of the result. All calls made up to {@link #stoppedAt(StreamPos, MatchPoint)} are considered potential candidates.
     */
    @Override
    void token(MatchResult token);

    /**
     * Accepts all input up to the given position. May be called zero or more times.
     */
    void matched(StreamPos endPos);

    /**
     * Indicates where matching stopped.
     *
     * @param nextExpression Can be null.
     */
    void stoppedAt(StreamPos stoppedAt, MatchPoint nextExpression);
}
