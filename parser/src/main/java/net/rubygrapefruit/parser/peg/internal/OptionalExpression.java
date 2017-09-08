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
        return expression.getMatcher().getPrefixes();
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        CharStream tail = stream.tail();
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        if (expression.getMatcher().consume(tail, nested)) {
            nested.forwardAll(expression.collector(visitor), visitor);
            stream.moveTo(tail);
        } else {
            visitor.matched(stream.current());
            nested.forwardRemainder(expression.collector(visitor), visitor);
        }
        return true;
    }
}
