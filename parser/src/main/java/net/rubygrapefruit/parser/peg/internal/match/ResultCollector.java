package net.rubygrapefruit.parser.peg.internal.match;

public interface ResultCollector {
    void token(MatchResult token);

    /**
     * Called when the result has been visited.
     */
    void done();
}
