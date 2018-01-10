package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OneOfExpression extends AbstractExpression implements Matcher, MatchPoint {
    private final List<? extends MatchExpression> expressions;

    public OneOfExpression(List<? extends MatchExpression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public String toString() {
        return "{one-of " + expressions + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean isAcceptEmpty() {
        for (MatchExpression expression : expressions) {
            if (expression.getMatcher().isAcceptEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<Terminal> getPrefixes() {
        Set<Terminal> prefixes = new HashSet<Terminal>();
        for (MatchExpression expression : expressions) {
            prefixes.addAll(expression.getMatcher().getPrefixes());
        }
        return prefixes;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        for (MatchExpression expression : expressions) {
            CharStream tail = stream.tail();
            BatchingMatchVisitor nested = new BatchingMatchVisitor();
            if (expression.getMatcher().consume(tail, nested)) {
                visitor.matched(nested);
                stream.moveTo(tail);
                return true;
            }
            visitor.attempted(nested);
        }
        return false;
    }
}
