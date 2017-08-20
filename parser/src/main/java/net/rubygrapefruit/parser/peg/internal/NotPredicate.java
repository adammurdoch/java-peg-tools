package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.Collections;
import java.util.Set;

public class NotPredicate implements Expression, MatchExpression, Matcher, MatchPoint {
    private final MatchExpression expression;

    public NotPredicate(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public Expression group() {
        return this;
    }

    @Override
    public boolean isAcceptEmpty() {
        return true;
    }

    @Override
    public Set<? extends Terminal> getPrefixes() {
        return Collections.emptySet();
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public ResultCollector collector(TokenCollector collector) {
        return new ResultCollector() {
            @Override
            public void token(TextRegion token) {
            }

            @Override
            public void done() {
            }
        };
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        // TODO - use a visitor that does nothing
        if (expression.getMatcher().consume(stream.tail(), new BatchingMatchVisitor())) {
            visitor.stoppedAt(stream.tail(), this);
            return false;
        } else {
            visitor.matched(stream.tail());
            return true;
        }
    }
}
