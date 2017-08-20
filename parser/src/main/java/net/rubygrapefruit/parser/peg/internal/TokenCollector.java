package net.rubygrapefruit.parser.peg.internal;

public interface TokenCollector {
    /**
     * Called when a matcher accepts some text.
     */
    void token(MatchResult token);
}
