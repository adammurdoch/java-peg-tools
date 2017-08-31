package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.Collections;
import java.util.Set;

public class CharSequenceExpression extends AbstractExpression implements Matcher, Terminal, MatchPoint {

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
        return getDisplayName();
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
        return "\"" + str.replace("\n", "\\n") + "\"";
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        StreamPos start = stream.current();
        if (stream.consume(str)) {
            StreamPos end = stream.current();
            visitor.token(new MatchResult(this, start, end));
            visitor.matched(end);
            return true;
        }
        visitor.stoppedAt(start, this);
        return false;
    }
}
