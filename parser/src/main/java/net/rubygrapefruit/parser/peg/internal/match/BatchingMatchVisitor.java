package net.rubygrapefruit.parser.peg.internal.match;

import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

import java.util.ArrayList;
import java.util.List;

public class BatchingMatchVisitor implements MatchVisitor, ExpressionMatchResult {
    private List<ExpressionMatchResult> results;
    private StreamPos matchEnd;
    private MatchPoint matchPoint;
    private ExpressionMatchResult best;

    @Override
    public void pushTokens(ResultCollector resultCollector) {
        if (results != null) {
            for (ExpressionMatchResult result : results) {
                result.pushTokens(resultCollector);
            }
            results.clear();
        }
    }

    @Override
    public StreamPos getMatchEnd() {
        return matchEnd;
    }

    @Override
    public StreamPos getStoppedAt() {
        return best.getStoppedAt();
    }

    @Override
    public MatchPoint getMatchPoint() {
        return matchPoint;
    }

    @Override
    public void attempted(StreamPos pos, MatchPoint expression) {
        attempted(new MatchNothingResult(pos, expression));
    }

    @Override
    public void attempted(ExpressionMatchResult result) {
        if (best == null || result.getStoppedAt().diff(best.getStoppedAt()) > 0) {
            matchPoint = result.getMatchPoint();
            best = result;
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
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(result);
        matchEnd = result.getMatchEnd();
        matchPoint = result.getMatchPoint();
        best = result;
    }

    private static class SingleTokenResult implements ExpressionMatchResult {
        private final MatchResult result;

        SingleTokenResult(MatchResult result) {
            this.result = result;
        }

        @Override
        public void pushTokens(ResultCollector resultCollector) {
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
        public void pushTokens(ResultCollector resultCollector) {
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
        public void pushTokens(ResultCollector resultCollector) {
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
}
