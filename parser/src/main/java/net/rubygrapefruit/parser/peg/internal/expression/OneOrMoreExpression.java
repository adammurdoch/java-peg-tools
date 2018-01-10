package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.internal.match.MatchExpression;
import net.rubygrapefruit.parser.peg.internal.match.Matcher;

import java.util.Arrays;

public class OneOrMoreExpression extends AbstractExpression {
    private final MatchExpression expression;
    private final Matcher matcher;

    public OneOrMoreExpression(MatchExpression expression) {
        this.expression = expression;
        matcher = new SequenceExpression(Arrays.asList(expression, new ZeroOrMoreExpression(expression))).getMatcher();
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
