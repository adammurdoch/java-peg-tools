package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.Collections;
import java.util.Set;

public class AnythingExpression extends AbstractExpression implements Matcher, Terminal, MatchPoint {
    @Override
    public Expression group() {
        return this;
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean isAcceptEmpty() {
        return false;
    }

    @Override
    public Set<? extends Terminal> getPrefixes() {
        return Collections.singleton(this);
    }

    @Override
    public String getDisplayName() {
        return "anything";
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        if (stream.isAtEnd()) {
            visitor.stoppedAt(stream.tail(), this);
            return false;
        }
        CharStream start = stream.tail();
        stream.consumeOne();
        CharStream end = stream.tail();
        visitor.token(start, end);
        visitor.matched(end);
        return true;
    }
}
