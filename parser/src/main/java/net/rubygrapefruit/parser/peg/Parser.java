package net.rubygrapefruit.parser.peg;

/**
 * Parses some input into a sequence of tokens.
 *
 * <p>Implementations are thread-safe.</p>
 */
public interface Parser {
    /**
     * Parses as much of the given string as possible.
     *
     * @param input The string to parse.
     * @param visitor The visitor to receive the results.
     * @return the visitor.
     */
    <T extends TokenVisitor> T parse(String input, T visitor);
}
