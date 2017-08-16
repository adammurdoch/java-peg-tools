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
        BatchingMatchVisitor nested = null;
        for (MatchExpression expression : expressions) {
            CharStream pos = stream.tail();
            CharStream start = stream.tail();
            nested = new BatchingMatchVisitor();
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
            if (pos.diff(start) == 0) {
                // Matched nothing but was successful, maybe keep it as the best partial match in case of failure
                if (partial == null || partial.getStoppedAt().diff(nested.getStoppedAt()) < 0) {
                    // Current expression recognized more than the previous partial match, keep it
                    partial = nested;
                }
                // Else, keep current partial match
            } else {
                // Matched something, assume it is the best match
                partial = null;
            }
        }
        visitor.matched(stream.tail(), nested.getStoppedAt());
        return true;
    }
}
