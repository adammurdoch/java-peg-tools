package net.rubygrapefruit.parser.peg.internal;

import java.util.List;

public interface Matcher {
    /**
     * Consume chars from the given stream and add tokens to the given list. Should not mutate either parameter when no match.
     *
     * @return true when this matcher was satisfied, false when not satisfied
     */
    boolean consume(CharStream stream, List<String> tokens);
}
