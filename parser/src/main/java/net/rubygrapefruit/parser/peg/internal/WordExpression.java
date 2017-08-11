package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class WordExpression implements Expression, Matcher {
    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        String token = stream.consumeUpTo(" ");
        if (token != null) {
            tokens.add(token);
            return true;
        }
        return false;
    }
}
