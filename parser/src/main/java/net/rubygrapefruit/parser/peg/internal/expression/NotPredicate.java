package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.internal.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;

import java.util.Collections;
import java.util.Set;

public class NotPredicate implements Expression, MatchExpression, Matcher, MatchPoint {
    private final MatchExpression expression;

    public NotPredicate(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "{not " + expression + "}";
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
            public void token(MatchResult token) {
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
            visitor.stoppedAt(stream.current(), this);
            return false;
        } else {
            visitor.matched(stream.current());
            return true;
        }
    }
}
