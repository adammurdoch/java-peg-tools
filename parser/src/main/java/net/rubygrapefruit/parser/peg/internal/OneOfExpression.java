package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.ArrayList;
import java.util.List;

public class OneOfExpression extends AbstractExpression implements Expression, MatchExpression, Matcher {
    private final List<Matcher> matchers;

    public OneOfExpression(List<? extends MatchExpression> expressions) {
        this.matchers = new ArrayList<Matcher>(expressions.size());
        for (MatchExpression expression : expressions) {
            matchers.add(expression.getMatcher());
        }
    }

    @Override
    public String toString() {
        return "{one-of " + matchers + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        BatchingMatchVisitor partialMatch = null;
        CharStream partialMatchEndsAt = null;
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        for (Matcher matcher : matchers) {
            CharStream pos = stream.tail();
            if (matcher.consume(pos, nested)) {
                nested.forward(visitor);
                stream.moveTo(pos);
                return true;
            }
            if (partialMatch == null || nested.matches() > partialMatch.matches()) {
                partialMatch = nested;
                partialMatchEndsAt = pos;
                nested = new BatchingMatchVisitor();
            } else {
                nested.reset();
            }
        }
        if (partialMatch != null) {
            partialMatch.forward(visitor);
            stream.moveTo(partialMatchEndsAt);
        }
        return false;
    }
}
