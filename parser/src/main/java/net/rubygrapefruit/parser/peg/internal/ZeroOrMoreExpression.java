package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

public class ZeroOrMoreExpression extends AbstractExpression implements Expression, Matcher {
    private final Matcher matcher;

    public ZeroOrMoreExpression(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public String toString() {
        return "{zero-or-more: " + matcher + "}";
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        while (matcher.consume(stream, visitor)) {
        }
        return true;
    }
}
