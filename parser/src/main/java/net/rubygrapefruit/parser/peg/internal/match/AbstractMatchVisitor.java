package net.rubygrapefruit.parser.peg.internal.match;

import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

import java.util.Collections;
import java.util.Set;

public abstract class AbstractMatchVisitor implements MatchVisitor {
    private static final NoAlternatives NO_ALTERNATIVES = new NoAlternatives();
    private StreamPos matchEnd;
    private StreamPos stoppedAt;
    private MatchPoint matchPoint;
    private ExpressionMatchResult bestAlternative;

    public StreamPos getMatchEnd() {
        return matchEnd;
    }

    public StreamPos getStoppedAt() {
        return stoppedAt;
    }

    public MatchPoint getMatchPoint() {
        return matchPoint == null ? NO_ALTERNATIVES : matchPoint;
    }

    @Override
    public void attempted(StreamPos pos, MatchPoint expression) {
        attempted(new MatchNothingResult(pos, expression));
    }

    @Override
    public void attempted(ExpressionMatchResult result) {
        if (stoppedAt == null) {
            matchPoint = result.getMatchPoint();
            stoppedAt = result.getStoppedAt();
            bestAlternative = result;
            return;
        }
        int diff = result.getStoppedAt().diff(stoppedAt);
        if (diff > 0) {
            matchPoint = result.getMatchPoint();
            stoppedAt = result.getStoppedAt();
            bestAlternative = result;
        } else if (diff == 0) {
            matchPoint = CompositeMatchPoint.of(matchPoint, result.getMatchPoint());
        }
    }

    @Override
    public void matched(StreamPos endPos) {
        matched(new EmptyMatchResult(endPos));
    }

    @Override
    public void matched(MatchResult result) {
        matched(new SingleTokenResult(result));
    }

    @Override
    public void matched(ExpressionMatchResult result) {
        addResult(result);
        matchEnd = result.getMatchEnd();
        stoppedAt = result.getStoppedAt();
        matchPoint = result.getMatchPoint();
        bestAlternative = null;
    }

    public void pushAll(ResultCollector resultCollector) {
        if (bestAlternative != null) {
            bestAlternative.pushAll(resultCollector);
            bestAlternative = null;
        }
    }

    protected abstract void addResult(ExpressionMatchResult result);

    private static class SingleTokenResult implements ExpressionMatchResult {
        private final MatchResult result;

        SingleTokenResult(MatchResult result) {
            this.result = result;
        }

        @Override
        public void pushMatches(ResultCollector resultCollector) {
            resultCollector.token(result);
        }

        @Override
        public void pushAll(ResultCollector resultCollector) {
            resultCollector.token(result);
        }

        @Override
        public StreamPos getMatchEnd() {
            return result.getEnd();
        }

        @Override
        public StreamPos getStoppedAt() {
            return result.getEnd();
        }

        @Override
        public MatchPoint getMatchPoint() {
            return null;
        }
    }

    private static class MatchNothingResult implements ExpressionMatchResult {
        private final StreamPos pos;
        private final MatchPoint expected;

        MatchNothingResult(StreamPos pos, MatchPoint expected) {
            this.pos = pos;
            this.expected = expected;
        }

        @Override
        public void pushMatches(ResultCollector resultCollector) {
        }

        @Override
        public void pushAll(ResultCollector resultCollector) {
        }

        @Override
        public StreamPos getMatchEnd() {
            return pos;
        }

        @Override
        public StreamPos getStoppedAt() {
            return pos;
        }

        @Override
        public MatchPoint getMatchPoint() {
            return expected;
        }
    }

    private static class EmptyMatchResult implements ExpressionMatchResult {
        private final StreamPos endPos;

        EmptyMatchResult(StreamPos endPos) {
            this.endPos = endPos;
        }

        @Override
        public void pushMatches(ResultCollector resultCollector) {
        }

        @Override
        public void pushAll(ResultCollector resultCollector) {
        }

        @Override
        public StreamPos getMatchEnd() {
            return endPos;
        }

        @Override
        public StreamPos getStoppedAt() {
            return endPos;
        }

        @Override
        public MatchPoint getMatchPoint() {
            return null;
        }
    }

    private static class NoAlternatives implements MatchPoint {
        @Override
        public Set<? extends Terminal> getPrefixes() {
            return Collections.emptySet();
        }
    }
}
