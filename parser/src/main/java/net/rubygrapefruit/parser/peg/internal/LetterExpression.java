package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class LetterExpression implements Expression, Matcher {
    @Override
    public Expression group() {
        return this;
    }

    @Override
    public String toString() {
        return "{letter}";
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        String token = stream.consumeLetter();
        if (token != null) {
            tokens.add(token);
            return true;
        }
        return false;
    }
}
