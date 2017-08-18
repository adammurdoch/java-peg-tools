package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

public class CharSequenceExpression extends AbstractExpression implements Matcher {
    private final String str;

    public CharSequenceExpression(String str) {
        this.str = str;
    }

    @Override
    public Expression group() {
        return this;
    }

    @Override
    public String toString() {
        return "\"" + str + "\"";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        CharStream start = stream.tail();
        if (stream.consume(str)) {
            CharStream end = stream.tail();
            visitor.token(start, end);
            visitor.matched(end);
            return true;
        }
        visitor.stoppedAt(start);
        return false;
    }
}
