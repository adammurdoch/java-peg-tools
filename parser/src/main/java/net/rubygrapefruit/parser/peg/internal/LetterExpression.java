package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

public class LetterExpression extends AbstractExpression implements Matcher {
    @Override
    public Expression group() {
        return this;
    }

    @Override
    public String toString() {
        return "{letter}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        CharStream start = stream.tail();
        String token = stream.consumeLetter();
        if (token != null) {
            CharStream end = stream.tail();
            visitor.token(start, end);
            visitor.matched(end);
            return true;
        }
        visitor.failed(start);
        return false;
    }
}
