package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class ZeroOrMoreExpression extends AbstractExpression implements Expression, Matcher {
    private final Matcher matcher;

    public ZeroOrMoreExpression(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        while (matcher.consume(stream, tokens)) {
        }
        return true;
    }
}
