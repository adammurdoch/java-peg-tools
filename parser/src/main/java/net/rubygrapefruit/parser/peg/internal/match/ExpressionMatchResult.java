package net.rubygrapefruit.parser.peg.internal.match;

import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

public interface ExpressionMatchResult {
    /**
     * Push the matches from this result to the given collector.
     */
    void pushMatches(ResultCollector resultCollector);

    /**
     * Push the matches and partial results from this result to the given collector.
     */
    void pushAll(ResultCollector resultCollector);

    StreamPos getMatchEnd();

    StreamPos getStoppedAt();

    MatchPoint getMatchPoint();
}
