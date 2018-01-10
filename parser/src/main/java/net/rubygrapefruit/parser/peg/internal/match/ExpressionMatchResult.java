package net.rubygrapefruit.parser.peg.internal.match;

import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

public interface ExpressionMatchResult extends TokenSource {
    /**
     * Returns all results including matches and partial matches.
     */
    TokenSource withBestAlternative();

    boolean hasPartialMatches();

    StreamPos getMatchEnd();

    StreamPos getStoppedAt();

    MatchPoint getMatchPoint();
}
