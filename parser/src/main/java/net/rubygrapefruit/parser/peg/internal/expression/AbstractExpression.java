package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.internal.match.MatchExpression;

public abstract class AbstractExpression implements Expression, MatchExpression {
    @Override
    public Expression group() {
        return new GroupingExpression(this);
    }
}
