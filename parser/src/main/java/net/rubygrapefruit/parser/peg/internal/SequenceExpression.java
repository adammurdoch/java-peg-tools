package net.rubygrapefruit.parser.peg.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SequenceExpression extends AbstractExpression implements Matcher {
    private final List<? extends MatchExpression> expressions;

    public SequenceExpression(List<? extends MatchExpression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public String toString() {
        return "{sequence: " + expressions + "}";
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean isAcceptEmpty() {
        for (MatchExpression expression : expressions) {
            if (!expression.isAcceptEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<? extends Terminal> getPrefixes() {
        Set<Terminal> prefixes = new HashSet<>();
        for (MatchExpression expression : expressions) {
            prefixes.addAll(expression.getPrefixes());
            if (!expression.isAcceptEmpty()) {
                break;
            }
        }
        return prefixes;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        CharStream startAll = stream.tail();
        BatchingMatchVisitor bestPartialMatch = null;
        MatchExpression bestPartialExpression = null;
        BatchingMatchVisitor nested = null;
        for (MatchExpression expression : expressions) {
            CharStream pos = stream.tail();
            CharStream start = stream.tail();
            nested = new BatchingMatchVisitor();
            boolean matched = expression.getMatcher().consume(pos, nested);
            stream.moveTo(pos);
            if (!matched) {
                if (bestPartialMatch == null || bestPartialMatch.getStoppedAt().diff(nested.getStoppedAt()) <= 0) {
                    bestPartialMatch = nested;
                    bestPartialExpression = expression;
                }
                if (bestPartialMatch.getStoppedAt().diff(startAll) == 0) {
                    bestPartialMatch.stoppedAt(startAll, this);
                }
                bestPartialMatch.forwardRemainder(bestPartialExpression.collector(visitor), visitor);
                return false;
            }
            nested.forwardMatches(expression.collector(visitor), visitor);
            if (pos.diff(start) == 0) {
                // Matched nothing but was successful, maybe keep it as the best partial match in case of failure
                if (bestPartialMatch == null || bestPartialMatch.getStoppedAt().diff(nested.getStoppedAt()) <= 0) {
                    // Current expression recognized more than the previous partial match, keep it
                    bestPartialMatch = nested;
                    bestPartialExpression = expression;
                }
                // Else, keep current partial match
            } else {
                // Matched something, assume it is the best match
                bestPartialMatch = nested;
                bestPartialExpression = expression;
            }
        }
        nested.forwardRemainder(bestPartialExpression.collector(visitor), visitor);
        return true;
    }
}
