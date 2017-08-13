package net.rubygrapefruit.parser.peg.internal;

public interface TokenCollector {
    /**
     * Called when a matcher accepts the given token.
     */
    void token(String token);
}
