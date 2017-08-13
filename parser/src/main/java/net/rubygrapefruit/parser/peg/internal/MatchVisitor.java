package net.rubygrapefruit.parser.peg.internal;

public interface MatchVisitor extends TokenCollector {
    /**
     * Called when a matcher stops matching.
     */
    void stoppedAt(CharStream pos);
}
