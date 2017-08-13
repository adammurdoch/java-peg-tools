package net.rubygrapefruit.parser.peg.internal;

public interface TokenCollector {
    /**
     * Called when a matcher accepts some text.
     *
     * @param start The start of the matching region.
     * @param end The end of the matching region, exclusive.
     */
    void token(CharStream start, CharStream end);
}
