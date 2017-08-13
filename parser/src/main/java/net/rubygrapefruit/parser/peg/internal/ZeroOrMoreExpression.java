package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

public class ZeroOrMoreExpression extends AbstractExpression implements Expression, MatchExpression, Matcher {
    private final Matcher matcher;

    public ZeroOrMoreExpression(MatchExpression expression) {
        this.matcher = expression.getMatcher();
    }

    @Override
    public String toString() {
        return "{zero-or-more: " + matcher + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        while (true) {
            CharStream pos = stream.tail();
            if (!matcher.consume(pos, nested)) {
                break;
            }
            stream.moveTo(pos);
            nested.forward(visitor);
        }
        return true;
    }
}
