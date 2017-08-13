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
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        for (MatchExpression expression : expressions) {
            CharStream pos = stream.tail();
            boolean matched = expression.getMatcher().consume(pos, nested);
            stream.moveTo(pos);
            nested.forward(expression, visitor);
            if (!matched) {
                return false;
            }
        }
        return true;
    }
}
