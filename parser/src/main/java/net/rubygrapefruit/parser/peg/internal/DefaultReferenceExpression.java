package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.ReferenceExpression;

import java.util.Set;

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
    public boolean isAcceptEmpty() {
        return expression.isAcceptEmpty();
    }

    @Override
    public Set<? extends Terminal> getPrefixes() {
        return expression.getPrefixes();
    }

    @Override
    public Expression group() {
        return new GroupingExpression(this);
    }
}
