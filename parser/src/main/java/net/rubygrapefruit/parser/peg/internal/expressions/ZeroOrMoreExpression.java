package net.rubygrapefruit.parser.peg.internal.expressions;

import net.rubygrapefruit.parser.peg.internal.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

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
        StreamPos stoppedAt = stream.current();
        MatchPoint matchPoint = null;
        while (true) {
            CharStream tail = stream.tail();
            boolean matched = expression.getMatcher().consume(tail, nested);
            int diff = nested.getStoppedAt().diff(stoppedAt);
            if (diff > 0) {
                // recognized more, assume it is the best choice
                stoppedAt = nested.getStoppedAt();
                matchPoint = nested.getMatchPoint();
            } else if (diff == 0) {
                // recognized same
                stoppedAt = nested.getStoppedAt();
                matchPoint = CompositeMatchPoint.of(matchPoint, nested.getMatchPoint());
            } // else, previous attempt recognized more
            if (!matched) {
                break;
            }
            nested.forwardMatches(expression.collector(visitor), visitor);
            stream.moveTo(tail);
        }
        visitor.matched(stream.current());
        nested.forwardRemainder(expression.collector(visitor), visitor);
        visitor.stoppedAt(stoppedAt, matchPoint);
        return true;
    }
}
