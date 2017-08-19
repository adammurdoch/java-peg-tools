package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.Collections;
import java.util.Set;

public class LetterExpression extends AbstractExpression implements Matcher, Terminal {
    @Override
    public Expression group() {
        return this;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public String getDisplayName() {
        return "{letter}";
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
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        CharStream start = stream.tail();
        String token = stream.consumeLetter();
        if (token != null) {
            CharStream end = stream.tail();
            visitor.token(start, end);
            visitor.matched(end);
            return true;
        }
        visitor.stoppedAt(start, this);
        return false;
    }
}
