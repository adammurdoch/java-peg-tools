package net.rubygrapefruit.parser.peg;

import java.util.List;

public interface Parser {
    /**
     * Parses as much of the given string as possible.
     *
     * @return the sequence of tokens.
     */
    List<String> parse(String input);
}
