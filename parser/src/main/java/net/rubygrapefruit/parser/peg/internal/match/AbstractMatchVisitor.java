package net.rubygrapefruit.parser.peg.internal.match;

import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

import java.util.ArrayList;
import java.util.List;

import static net.rubygrapefruit.parser.peg.internal.match.MatchPoint.*;

public abstract class AbstractMatchVisitor implements MatchVisitor {
    private StreamPos matchEnd;
    private StreamPos stoppedAt;
    private MatchPoint matchPoint;
    private TokenSource bestAlternative;
    private List<ExpressionMatchResult> pending;

    public StreamPos getMatchEnd() {
        return matchEnd;
    }

    public StreamPos getStoppedAt() {
        return stoppedAt;
    }

    public MatchPoint getMatchPoint() {
        return matchPoint == null ? NO_ALTERNATIVES : matchPoint;
    }

    public boolean hasPartialMatches() {
        return bestAlternative != null || (pending != null && !pending.isEmpty());
    }

    protected void commitMatching() {
        if (pending != null) {
            for (ExpressionMatchResult result : pending) {
                commit(result);
            }
            pending.clear();
        }
    }

    public void commitPartialMatches() {
        if (bestAlternative != null) {
            commit(bestAlternative);
            if (pending != null) {
                pending.clear();
            }
            bestAlternative = null;
        }
        if (pending != null && !pending.isEmpty()) {
            for (int i = 0; i < pending.size() - 1; i++) {
                ExpressionMatchResult result = pending.get(i);
                commit(result);
            }
            commit(pending.get(pending.size()-1).withBestAlternative());
            pending.clear();
        }
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
            bestAlternative = result.withBestAlternative();
            return;
        }
        int diff = result.getStoppedAt().diff(stoppedAt);
        if (diff > 0) {
            // This alternative has made more progress than matches
            matchPoint = result.getMatchPoint();
            stoppedAt = result.getStoppedAt();
            bestAlternative = result.withBestAlternative();
        } else if (diff == 0) {
            // This alternative has made the same progress as other alternatives
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
        matchEnd = result.getMatchEnd();
        if (stoppedAt == null) {
            stoppedAt = result.getStoppedAt();
            matchPoint = result.getMatchPoint();
            acceptMatch(result);
            return;
        }
        int diff = result.getStoppedAt().diff(stoppedAt);
        if (diff > 0) {
            // This match has made the most progress
            stoppedAt = result.getStoppedAt();
            matchPoint = result.getMatchPoint();
            bestAlternative = null;
            acceptMatch(result);
        } else if (diff == 0) {
            // This match has made the same amount of progress
            matchPoint = CompositeMatchPoint.of(matchPoint, result.getMatchPoint());
            bestAlternative = null;
            acceptMatch(result);
        } else {
            // An alternative has made more progress, keep this
            queueMatch(result);
        }
    }

    private void acceptMatch(ExpressionMatchResult result) {
        if (pending != null) {
            for (ExpressionMatchResult batched : pending) {
                commit(batched);
            }
            pending.clear();
        }
        if (result.hasPartialMatches()) {
            queueMatch(result);
        } else {
            commit(result);
        }
    }

    private void queueMatch(ExpressionMatchResult result) {
        if (pending == null) {
            pending = new ArrayList<>();
        }
        pending.add(result);
    }

    /**
     * Adds the given result to the matches.
     */
    protected abstract void commit(TokenSource result);

    private static class SingleTokenResult implements ExpressionMatchResult {
        private final MatchResult result;

        SingleTokenResult(MatchResult result) {
            this.result = result;
        }

        @Override
        public void pushMatches(TokenCollector resultCollector) {
            resultCollector.token(result);
        }

        @Override
        public TokenSource withBestAlternative() {
            return this;
        }

        @Override
        public boolean hasPartialMatches() {
            return false;
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
        public void pushMatches(TokenCollector resultCollector) {
        }

        @Override
        public TokenSource withBestAlternative() {
            return EMPTY;
        }

        @Override
        public boolean hasPartialMatches() {
            return false;
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
        public void pushMatches(TokenCollector resultCollector) {
        }

        @Override
        public TokenSource withBestAlternative() {
            return EMPTY;
        }

        @Override
        public boolean hasPartialMatches() {
            return false;
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
