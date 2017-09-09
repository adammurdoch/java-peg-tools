package net.rubygrapefruit.parser.peg;

/**
 * Represents a back reference to the result of a expression.
 */
public interface BackReference {
    /**
     * Returns an expression that matches the value of the source expression.
     */
    Expression getValue();

    /**
     * Returns an expression that matches the source expression followed by the given expression. The back reference can be used by the following expression.
     */
    Expression followedBy(Expression expression);
}
