package net.rubygrapefruit.parser.peg.internal;

public interface ResultCollector {
    void token(TextRegion token);

    /**
     * Called when the result has been visited.
     */
    void done();
}
