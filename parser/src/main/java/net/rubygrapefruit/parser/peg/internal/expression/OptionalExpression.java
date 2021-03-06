package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;

import java.util.Set;

public class OptionalExpression extends AbstractExpression implements Matcher {
    private final MatchExpression expression;

    public OptionalExpression(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "{optional: " + expression + "}";
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
        CharStream tail = stream.tail();
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        if (expression.getMatcher().consume(tail, nested)) {
            visitor.matched(nested);
            stream.moveTo(tail);
        } else {
            visitor.attempted(nested);
            visitor.matched(stream.current());
        }
        return true;
    }
}
