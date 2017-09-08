package net.rubygrapefruit.parser.peg;

/**
 * A mutable reference to some other expression. This can be used to construct recursive expressions.
 */
public interface ReferenceExpression extends Expression {
    void set(Expression expression);
}
