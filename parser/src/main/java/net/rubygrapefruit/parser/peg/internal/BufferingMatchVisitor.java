package net.rubygrapefruit.parser.peg.internal;

public interface BufferingMatchVisitor extends MatchVisitor {
    /**
     * Called when the result has been visited.
     */
    void done();
}
