package net.rubygrapefruit.parser.peg;

public interface TokenVisitor {
    /**
     * Called when a token is matched.
     */
    void token(String token);

    /**
     * Called on end of stream.
     */
    void end();
}
