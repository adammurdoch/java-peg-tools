package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.ArrayList;
import java.util.List;

public class SequenceExpression extends AbstractExpression implements Expression, MatchExpression, Matcher {
    private final List<Matcher> matchers;

    public SequenceExpression(List<? extends MatchExpression> expressions) {
        this.matchers = new ArrayList<Matcher>(expressions.size());
        for (MatchExpression expression : expressions) {
            matchers.add(expression.getMatcher());
        }
    }

    @Override
    public String toString() {
        return "{sequence: " + matchers + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        for (Matcher matcher : matchers) {
            CharStream pos = stream.tail();
            boolean matched = matcher.consume(pos, nested);
            stream.moveTo(pos);
            nested.forward(visitor);
            if (!matched) {
                return false;
            }
        }
        return true;
    }
}
