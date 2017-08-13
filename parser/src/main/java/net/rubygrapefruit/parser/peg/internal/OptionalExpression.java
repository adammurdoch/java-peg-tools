package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

public class OptionalExpression extends AbstractExpression implements Expression, MatchExpression, Matcher {
    private final Matcher matcher;

    public OptionalExpression(MatchExpression expression) {
        this.matcher = expression.getMatcher();
    }

    @Override
    public String toString() {
        return "{optional: " + matcher + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        CharStream pos = stream.tail();
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        if (matcher.consume(pos, nested)) {
            nested.forward(visitor);
            stream.moveTo(pos);
        }
        return true;
    }
}
