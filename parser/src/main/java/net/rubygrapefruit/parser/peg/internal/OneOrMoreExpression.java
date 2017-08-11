package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.ArrayList;
import java.util.List;

public class OneOrMoreExpression extends AbstractExpression implements Expression, Matcher {
    private final Matcher matcher;

    public OneOrMoreExpression(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public String toString() {
        return "{one-or-more: " + matcher + "}";
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        if (!matcher.consume(stream, visitor)) {
            return false;
        }
        while (matcher.consume(stream, visitor)) {
        }
        return true;
    }
}
