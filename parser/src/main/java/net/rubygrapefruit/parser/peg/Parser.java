package net.rubygrapefruit.parser.peg;

/**
 * Parses some input into a sequence of tokens. For now, this is a scanner rather than a parser.
 *
 * <p>Implementations are thread-safe.</p>
 */
public interface Parser {
    /**
     * Parses as much of the given string as possible, forwarding the results to the given visitor.
     *
     * @param input The string to parse.
     * @param visitor The visitor to receive the results.
     * @return the visitor.
     */
    <T extends TokenVisitor<Expression>> T parse(String input, T visitor);
}
