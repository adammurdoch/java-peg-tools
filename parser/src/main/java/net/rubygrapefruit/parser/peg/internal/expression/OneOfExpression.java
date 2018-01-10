package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

import java.util.ArrayList;
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
        StreamPos start = stream.current();
        StreamPos bestPos = null;
        List<Candidate> candidates = new ArrayList<Candidate>(3);
        for (MatchExpression expression : expressions) {
            CharStream tail = stream.tail();
            BatchingMatchVisitor nested = new BatchingMatchVisitor();
            if (expression.getMatcher().consume(tail, nested)) {
                nested.forwardAll(expression.collector(visitor), visitor);
                if (bestPos != null && bestPos.diff(nested.getStoppedAt()) == 0) {
                    candidates.add(new Candidate(expression, nested));
                    visitor.stoppedAt(bestPos, mergedOptions(candidates));
                }
                stream.moveTo(tail);
                return true;
            }
            if (bestPos == null || nested.getStoppedAt().diff(bestPos) > 0) {
                bestPos = nested.getStoppedAt();
                candidates.clear();
                candidates.add(new Candidate(expression, nested));
            } else if (nested.getStoppedAt().diff(bestPos) == 0) {
                candidates.add(new Candidate(expression, nested));
            }
        }
        Candidate bestOption = candidates.get(candidates.size() - 1);
        bestOption.result.forwardRemainder(bestOption.expression.collector(visitor), visitor);
        if (start.diff(bestPos) == 0) {
            visitor.stoppedAt(start, this);
        } else if (candidates.size() > 1) {
            visitor.stoppedAt(bestPos, mergedOptions(candidates));
        }
        return false;
    }

    private MatchPoint mergedOptions(List<Candidate> candidates) {
        final List<MatchPoint> points = new ArrayList<MatchPoint>(candidates.size());
        for (Candidate candidate : candidates) {
            points.add(candidate.result.getMatchPoint());
        }
        return new CompositeMatchPoint(points);
    }

    private static class Candidate {
        final BatchingMatchVisitor result;
        final MatchExpression expression;

        Candidate(MatchExpression expression, BatchingMatchVisitor result) {
            this.result = result;
            this.expression = expression;
        }
    }
}
