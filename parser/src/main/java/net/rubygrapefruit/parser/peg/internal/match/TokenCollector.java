package net.rubygrapefruit.parser.peg.internal.match;

public interface TokenCollector {
    /**
     * Called when a matcher accepts some text.
     */
    void token(MatchResult token);
}
