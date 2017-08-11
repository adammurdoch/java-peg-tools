package net.rubygrapefruit.parser.peg;

import java.util.List;

/**
 * Parses some input into a sequence of tokens.
 *
 * <p>Implementations are thread-safe.</p>
 */
public interface Parser {
    /**
     * Parses as much of the given string as possible.
     *
     * @return the sequence of tokens.
     */
    List<String> parse(String input);
}
