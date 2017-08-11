package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class OptionalExpression extends AbstractExpression implements Expression, Matcher {
    private final Matcher matcher;

    public OptionalExpression(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        matcher.consume(stream, tokens);
        return true;
    }
}
