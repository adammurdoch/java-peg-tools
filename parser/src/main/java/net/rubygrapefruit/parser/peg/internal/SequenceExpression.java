package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class SequenceExpression extends AbstractExpression implements Expression, Matcher {
    private final List<Matcher> matchers;

    public SequenceExpression(List<Matcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public String toString() {
        return "{sequence: " + matchers + "}";
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        for (Matcher matcher : matchers) {
            CharStream pos = stream.tail();
            if (!matcher.consume(pos, nested)) {
                return false;
            }
            stream.moveTo(pos);
            nested.forward(visitor);
        }
        return true;
    }
}
