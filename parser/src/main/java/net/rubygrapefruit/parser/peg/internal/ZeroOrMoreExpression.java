package net.rubygrapefruit.parser.peg.internal;

public class ZeroOrMoreExpression extends AbstractExpression implements Matcher {
    private final MatchExpression expression;

    public ZeroOrMoreExpression(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "{zero-or-more: " + expression + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        while (true) {
            CharStream pos = stream.tail();
            if (!expression.getMatcher().consume(pos, nested)) {
                break;
            }
            stream.moveTo(pos);
            nested.forward(expression, visitor);
        }
        return true;
    }
}
