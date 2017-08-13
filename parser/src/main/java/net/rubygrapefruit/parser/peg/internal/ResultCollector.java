package net.rubygrapefruit.parser.peg.internal;

public interface ResultCollector {
    void token(String token);

    /**
     * Called when the result has been visited.
     */
    void done();
}
