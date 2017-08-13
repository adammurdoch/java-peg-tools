package net.rubygrapefruit.parser.peg.internal;

public class OptionalExpression extends AbstractExpression implements Matcher {
    private final MatchExpression expression;

    public OptionalExpression(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "{optional: " + expression + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        CharStream pos = stream.tail();
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        if (expression.getMatcher().consume(pos, nested)) {
            nested.forward(expression.collector(visitor));
            stream.moveTo(pos);
        }
        return true;
    }
}
