package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.internal.stream.CharStream;

import java.util.Set;

public interface Matcher {
    /**
     * Can this matcher accept and consume no input?
     */
    boolean isAcceptEmpty();

    Set<? extends Terminal> getPrefixes();

    /**
     * Consume chars from the given stream and add tokens to the given list.
     *
     * @param stream The stream to read from. Should not consume beyond the end of the match.
     * @param visitor The visitor to push tokens to. May push tokens on partial match.
     * @return true when this matcher was satisfied, false when not satisfied
     */
    boolean consume(CharStream stream, MatchVisitor visitor);
}
