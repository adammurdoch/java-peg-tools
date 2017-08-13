package net.rubygrapefruit.parser.peg.internal;

import java.util.Arrays;

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
}
