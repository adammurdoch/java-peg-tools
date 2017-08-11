package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class SingleCharExpression implements Expression, Matcher {
    private final String token;

    public SingleCharExpression(char ch) {
        token = Character.toString(ch);
    }

    @Override
    public Expression group() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        if (stream.consume(token)) {
            tokens.add(token);
            return true;
        }
        return false;
    }
}
