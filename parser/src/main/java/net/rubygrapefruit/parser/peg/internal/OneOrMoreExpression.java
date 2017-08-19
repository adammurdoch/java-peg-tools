package net.rubygrapefruit.parser.peg.internal;

import java.util.Arrays;
import java.util.Set;

public class OneOrMoreExpression extends AbstractExpression {
    private final MatchExpression expression;
    private final Matcher matcher;

    public OneOrMoreExpression(MatchExpression expression) {
        this.expression = expression;
        matcher = new SequenceExpression(Arrays.asList(expression, new ZeroOrMoreExpression(expression)));
    }

    @Override
    public String toString() {
        return "{one-or-more: " + expression + "}";
    }

    @Override
    public Matcher getMatcher() {
        return matcher;
    }

    @Override
    public boolean isAcceptEmpty() {
        return expression.isAcceptEmpty();
    }

    @Override
    public Set<? extends Terminal> getPrefixes() {
        return expression.getPrefixes();
    }
}
