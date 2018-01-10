package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

import java.util.Collections;
import java.util.Set;

public class AnythingExpression extends AbstractExpression implements Matcher, Terminal, MatchPoint {
    @Override
    public String toString() {
        return "{anything}";
    }

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
        StreamPos start = stream.current();
        if (stream.consumeOne()) {
            StreamPos end = stream.current();
            visitor.token(new MatchResult(this, start, end));
            visitor.matched(end);
            return true;
        }
        visitor.stoppedAt(start, this);
        return false;
    }
}
