package net.rubygrapefruit.parser.peg;

public interface TokenVisitor<T> {
    /**
     * Called when a token is matched. A token is defined as the match for the outermost expression created using {@link Expression#group()} or any terminal expression, such as {@link ParserBuilder#chars(String)}, that is not contained in such an expression.
     *
     * @param type The type of token.
     * @param match The region of text that matched, possibly empty.
     */
    void token(T type, Region match);

    /**
     * Called when parsing stops due to a failure to match.
     * @param message The error message.
     * @param remainder The region from the point where parsing stopped to the end of the input.
     */
    void failed(String message, Region remainder);
}
