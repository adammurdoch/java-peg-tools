package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

public class OptionalExpression extends AbstractExpression implements Expression, Matcher {
    private final Matcher matcher;

    public OptionalExpression(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public String toString() {
        return "{optional: " + matcher + "}";
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        matcher.consume(stream, visitor);
        return true;
    }
}
