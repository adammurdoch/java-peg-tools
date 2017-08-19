package net.rubygrapefruit.parser.peg.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OneOfExpression extends AbstractExpression implements Matcher {
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
            if (expression.isAcceptEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<Terminal> getPrefixes() {
        Set<Terminal> prefixes = new HashSet<Terminal>();
        for (MatchExpression expression : expressions) {
            prefixes.addAll(expression.getPrefixes());
        }
        return prefixes;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        CharStream start = stream.tail();
        BatchingMatchVisitor bestMatch = null;
        MatchExpression bestMatchExpression = null;
        BatchingMatchVisitor nested = new BatchingMatchVisitor();
        for (MatchExpression expression : expressions) {
            CharStream pos = stream.tail();
            if (expression.getMatcher().consume(pos, nested)) {
                nested.forwardAll(expression.collector(visitor), visitor);
                stream.moveTo(pos);
                return true;
            }
            if (bestMatch == null || bestMatch.getStoppedAt().diff(nested.getStoppedAt()) <= 0) {
                bestMatch = nested;
                bestMatchExpression = expression;
                nested = new BatchingMatchVisitor();
            } else {
                nested.reset();
            }
        }
        if (start.diff(bestMatch.getStoppedAt()) == 0) {
            bestMatch.stoppedAt(start, this);
        }
        bestMatch.forwardRemainder(bestMatchExpression.collector(visitor), visitor);
        return false;
    }
}
