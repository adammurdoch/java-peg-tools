package net.rubygrapefruit.parser.peg.internal.match;

import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

public interface ExpressionMatchResult {
    void pushTokens(ResultCollector resultCollector);

    StreamPos getMatchEnd();

    StreamPos getStoppedAt();

    MatchPoint getMatchPoint();
}
