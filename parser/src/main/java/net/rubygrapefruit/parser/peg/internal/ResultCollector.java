package net.rubygrapefruit.parser.peg.internal;

public interface ResultCollector {
    /**
     * @param start The start of the matching region.
     * @param end The end of the matching region, exclusive.
     */
    void token(CharStream start, CharStream end);

    /**
     * Called when the result has been visited.
     */
    void done();
}
