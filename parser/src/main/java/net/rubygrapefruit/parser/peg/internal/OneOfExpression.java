package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class OneOfExpression extends AbstractExpression implements Expression, Matcher {
    private final List<? extends Matcher> matchers;

    public OneOfExpression(List<? extends Matcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public String toString() {
        return "{one-of " + matchers + "}";
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        for (Matcher matcher : matchers) {
            CharStream pos = stream.tail();
            nested.reset();
            if (matcher.consume(pos, nested)) {
                nested.forward(visitor);
                stream.moveTo(pos);
                return true;
            }
        }
        return false;
    }
}