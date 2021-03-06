package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

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
            visitor.matched(new MatchResult(this, start, end));
            return true;
        }
        visitor.attempted(start, this);
        return false;
    }
}
