package net.rubygrapefruit.parser.peg.internal;

import java.util.Set;

public class OptionalExpression extends AbstractExpression implements Matcher {
    private final MatchExpression expression;

    public OptionalExpression(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "{optional: " + expression + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean isAcceptEmpty() {
        return true;
    }

    @Override
    public Set<? extends Terminal> getPrefixes() {
        return expression.getPrefixes();
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        CharStream pos = stream.tail();
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        if (expression.getMatcher().consume(pos, nested)) {
            nested.forwardAll(expression.collector(visitor), visitor);
            stream.moveTo(pos);
        } else {
            visitor.matched(stream.tail());
            nested.forwardRemainder(expression.collector(visitor), visitor);
        }
        return true;
    }
}
