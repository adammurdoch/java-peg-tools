package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

public abstract class AbstractExpression implements Expression, Matcher {
    @Override
    public Expression group() {
        return new GroupingExpression(this);
    }
}
