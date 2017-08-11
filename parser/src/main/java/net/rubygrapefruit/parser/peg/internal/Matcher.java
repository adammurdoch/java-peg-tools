package net.rubygrapefruit.parser.peg.internal;

public interface Matcher {
    /**
     * Consume chars from the given stream and add tokens to the given list.
     *
     * @param stream The stream to read from. May consume partial match. Should not consume beyond match
     * @param visitor The visitor to push tokens to. May push tokens on partial match. Should not push beyond match
     * @return true when this matcher was satisfied, false when not satisfied
     */
    boolean consume(CharStream stream, MatchVisitor visitor);
}
