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
        BatchingMatchVisitor bestMatch = null;
        MatchExpression bestMatchExpression = null;
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        for (MatchExpression expression : expressions) {
            CharStream pos = stream.tail();
            if (expression.getMatcher().consume(pos, nested)) {
                nested.forward(expression.collector(visitor));
                stream.moveTo(pos);
                visitor.matched(pos, nested.getStoppedAt());
                return true;
            }
            if (bestMatch == null || bestMatch.getStoppedAt().diff(nested.getStoppedAt()) <= 0) {
                bestMatch = nested;
                bestMatchExpression = expression;
                nested = new BatchingMatchVisitor();
            } else {
                nested.reset();
            }
        }
        bestMatch.forward(bestMatchExpression.collector(visitor));
        visitor.failed(bestMatch.getStoppedAt());
        return false;
    }
}
