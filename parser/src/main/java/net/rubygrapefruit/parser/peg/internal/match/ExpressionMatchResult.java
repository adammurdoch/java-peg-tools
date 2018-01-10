package net.rubygrapefruit.parser.peg.internal.match;

import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

public interface ExpressionMatchResult {
    /**
     * Push the matches from this result to the given collector.
     */
    void pushMatches(ResultCollector resultCollector);

    void acceptBestAlternative();

    StreamPos getMatchEnd();

    StreamPos getStoppedAt();

    MatchPoint getMatchPoint();
}
