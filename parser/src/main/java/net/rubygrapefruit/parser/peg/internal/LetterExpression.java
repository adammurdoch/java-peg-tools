package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

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
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        String token = stream.consumeLetter();
        if (token != null) {
            visitor.token(token);
            return true;
        }
        return false;
    }
}
