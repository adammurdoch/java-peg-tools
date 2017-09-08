package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.ReferenceExpression;

public class DefaultReferenceExpression implements ReferenceExpression, MatchExpression {
    private MatchExpression expression;

    @Override
    public String toString() {
        return "{ref: " + expression + "}";
    }

    @Override
    public void set(Expression expression) {
        this.expression = (MatchExpression) expression;
    }

    @Override
    public ResultCollector collector(TokenCollector collector) {
        return expression.collector(collector);
    }

    @Override
    public Matcher getMatcher() {
        return expression.getMatcher();
    }

    @Override
    public Expression group() {
        return new GroupingExpression(this);
    }
}
