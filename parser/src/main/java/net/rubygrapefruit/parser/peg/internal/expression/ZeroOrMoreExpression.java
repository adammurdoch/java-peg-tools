package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;

import java.util.Set;

public class ZeroOrMoreExpression extends AbstractExpression implements Matcher {
    private final MatchExpression expression;

    public ZeroOrMoreExpression(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "{zero-or-more: " + expression + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean isAcceptEmpty() {
        return true;
    }

    @Override
    public Set<? extends Terminal> getPrefixes() {
        return expression.getMatcher().getPrefixes();
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        while (true) {
            CharStream tail = stream.tail();
            BatchingMatchVisitor nested = new BatchingMatchVisitor();
            boolean matched = expression.getMatcher().consume(tail, nested);
            if (!matched) {
                visitor.attempted(nested);
                break;
            }
            visitor.matched(nested);
            stream.moveTo(tail);
        }
        visitor.matched(stream.current());
        return true;
    }
}
