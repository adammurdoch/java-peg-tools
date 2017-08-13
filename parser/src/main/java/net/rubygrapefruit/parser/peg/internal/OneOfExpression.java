package net.rubygrapefruit.parser.peg.internal;

import java.util.List;

public class OneOfExpression extends AbstractExpression implements Matcher {
    private final List<? extends MatchExpression> expressions;

    public OneOfExpression(List<? extends MatchExpression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public String toString() {
        return "{one-of " + expressions + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        BatchingMatchVisitor partialMatch = null;
        MatchExpression partialMatchExpression = null;
        CharStream partialMatchEndsAt = null;
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        for (MatchExpression expression : expressions) {
            CharStream pos = stream.tail();
            if (expression.getMatcher().consume(pos, nested)) {
                nested.forward(expression, visitor);
                stream.moveTo(pos);
                return true;
            }
            if (partialMatch == null || nested.matches() > partialMatch.matches()) {
                partialMatch = nested;
                partialMatchExpression = expression;
                partialMatchEndsAt = pos;
                nested = new BatchingMatchVisitor();
            } else {
                nested.reset();
            }
        }
        if (partialMatch != null) {
            partialMatch.forward(partialMatchExpression, visitor);
            stream.moveTo(partialMatchEndsAt);
        }
        return false;
    }
}
