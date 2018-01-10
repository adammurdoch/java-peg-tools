package net.rubygrapefruit.parser.peg.internal.match;

public interface TokenSource {
    /**
     * Push the matches from this result to the given collector.
     */
    void pushMatches(ResultCollector resultCollector);
}
