package net.rubygrapefruit.parser.peg.internal.match;

import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

public interface ExpressionMatchResult extends TokenSource {
    /**
     * Returns all results including matches and best alternative.
     */
    TokenSource withBestAlternative();

    /**
     * Returns only the best alternative and no matches.
     */
    TokenSource getBestAlternative();

    StreamPos getMatchEnd();

    StreamPos getStoppedAt();

    MatchPoint getMatchPoint();
}
