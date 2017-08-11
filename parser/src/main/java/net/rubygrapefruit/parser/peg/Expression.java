package net.rubygrapefruit.parser.peg;

public interface Expression {
    /**
     * Groups the contents of this expression into a single token.
     */
    Expression group();
}
