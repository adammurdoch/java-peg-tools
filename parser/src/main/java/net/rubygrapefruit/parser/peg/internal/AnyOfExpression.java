package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class AnyOfExpression extends AbstractExpression implements Expression, Matcher {
    private final List<? extends Matcher> matchers;

    public AnyOfExpression(List<? extends Matcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        for (Matcher matcher : matchers) {
            if (matcher.consume(stream, tokens)) {
                return true;
            }
        }
        return false;
    }
}
