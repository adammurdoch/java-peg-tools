package net.rubygrapefruit.parser.peg;

public interface TokenVisitor {
    /**
     * Called when a token is matched.
     */
    void token(Region match);

    /**
     * Called when parsing stops due to a failure to match.
     */
    void failed(String message, Region remainder);
}
