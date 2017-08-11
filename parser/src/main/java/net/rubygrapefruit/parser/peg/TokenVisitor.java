package net.rubygrapefruit.parser.peg;

public interface TokenVisitor {
    /**
     * Called when a token is matched.
     */
    void token(String token);

    /**
     * Called when parsing stops due to a failure to match.
     */
    void failed(String message);
}
