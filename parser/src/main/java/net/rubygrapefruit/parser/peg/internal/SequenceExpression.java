package net.rubygrapefruit.parser.peg.internal;

import java.util.List;

public class SequenceExpression extends AbstractExpression implements Matcher {
    private final List<? extends MatchExpression> expressions;

    public SequenceExpression(List<? extends MatchExpression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public String toString() {
        return "{sequence: " + expressions + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        BatchingMatchVisitor partial = null;
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        for (MatchExpression expression : expressions) {
            CharStream pos = stream.tail();
            CharStream start = stream.tail();
            boolean matched = expression.getMatcher().consume(pos, nested);
            stream.moveTo(pos);
            nested.forward(expression.collector(visitor));
            if (!matched) {
                if (partial != null && partial.getStoppedAt().diff(nested.getStoppedAt()) > 0) {
                    visitor.failed(partial.getStoppedAt());
                } else {
                    visitor.failed(nested.getStoppedAt());
                }
                return false;
            }
            // This isn't right, should select best choice out of partials if there's a chain of optionals
            if (pos.diff(start) == 0 && nested.getStoppedAt().diff(start) > 0) {
                // Matched nothing but recognized something
                partial = nested;
                nested = new BatchingMatchVisitor();
            } else {
                partial = null;
            }
        }
        visitor.matched(stream.tail(), nested.getStoppedAt());
        return true;
    }
}
