package net.rubygrapefruit.parser.peg.internal;

import java.util.Set;

public class ZeroOrMoreExpression extends AbstractExpression implements Matcher {
    private final MatchExpression expression;

    public ZeroOrMoreExpression(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "{zero-or-more: " + expression + "}";
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
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        while (true) {
            CharStream tail = stream.tail();
            if (!expression.getMatcher().consume(tail, nested)) {
                break;
            }
            nested.forwardMatches(expression.collector(visitor), visitor);
            stream.moveTo(tail);
        }
        visitor.matched(stream.current());
        nested.forwardRemainder(expression.collector(visitor), visitor);
        return true;
    }
}
