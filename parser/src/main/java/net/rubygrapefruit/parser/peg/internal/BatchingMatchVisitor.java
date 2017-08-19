package net.rubygrapefruit.parser.peg.internal;

import java.util.ArrayList;
import java.util.List;

public class BatchingMatchVisitor implements MatchVisitor {
    private List<Match> tokens;
    private List<Match> partialTokens;
    private CharStream matchEnd;
    private CharStream stoppedAt;
    private MatchPoint matchPoint;

    @Override
    public void token(CharStream start, CharStream end) {
        if (partialTokens == null) {
            partialTokens = new ArrayList<Match>();
        }
        partialTokens.add(new Match(start, end));
    }

    public CharStream getMatchEnd() {
        return matchEnd;
    }

    public MatchPoint getMatchPoint() {
        return matchPoint;
    }

    public CharStream getStoppedAt() {
        return stoppedAt;
    }

    @Override
    public void matched(CharStream endPos) {
        matchEnd = endPos;
        stoppedAt = endPos;
        if (partialTokens != null && !partialTokens.isEmpty()) {
            if (tokens == null) {
                tokens = new ArrayList<Match>(partialTokens.size());
            }
            tokens.addAll(partialTokens);
            partialTokens.clear();
        }
    }

    public void reset() {
        if (partialTokens != null) {
            partialTokens.clear();
        }
        if (tokens != null) {
            tokens.clear();
        }
        stoppedAt = null;
        matchEnd = null;
    }

    @Override
    public void stoppedAt(CharStream stoppedAt, MatchPoint matchPoint) {
        this.stoppedAt = stoppedAt;
        this.matchPoint = matchPoint;
    }

    /**
     * Forwards successful match state (matches, match pos) to the given collector.
     */
    public void forwardMatches(ResultCollector collector, MatchVisitor visitor) {
        if (matchEnd == null) {
            throw new IllegalStateException("No matches");
        }
        if (tokens != null) {
            for (Match token : tokens) {
                collector.token(token.start, token.end);
            }
            tokens.clear();
        }
        collector.done();
        visitor.matched(matchEnd);
    }

    private void forwardPartialMatch(ResultCollector collector, MatchVisitor visitor) {
        if (stoppedAt == null) {
            throw new IllegalStateException("No stop position");
        }
        if (partialTokens != null) {
            for (Match token : partialTokens) {
                collector.token(token.start, token.end);
            }
            partialTokens.clear();
        }
        collector.done();
        visitor.stoppedAt(stoppedAt, matchPoint);
    }

    /**
     * Forwards all match state (matches, match pos, partial matches, stop pos) to the given collector.
     */
    public void forwardAll(ResultCollector collector, MatchVisitor visitor) {
        forwardMatches(collector, visitor);
        forwardPartialMatch(collector, visitor);
    }

    public void forwardAll(MatchVisitor visitor) {
        if (matchEnd == null) {
            throw new IllegalStateException("No matches");
        }
        if (tokens != null) {
            for (Match token : tokens) {
                visitor.token(token.start, token.end);
            }
            tokens.clear();
        }
        visitor.matched(matchEnd);
        if (partialTokens != null) {
            for (Match token : partialTokens) {
                visitor.token(token.start, token.end);
            }
            partialTokens.clear();
        }
        visitor.stoppedAt(stoppedAt, matchPoint);
    }

    /**
     * Forwards remaining match state (matched, partial matches, stop pos) to the given collector.
     */
    public void forwardRemainder(ResultCollector collector, MatchVisitor visitor) {
        if (stoppedAt == null) {
            throw new IllegalStateException("No stop position");
        }
        if (tokens != null) {
            for (Match token : tokens) {
                collector.token(token.start, token.end);
            }
            tokens.clear();
        }
        if (partialTokens != null) {
            for (Match token : partialTokens) {
                collector.token(token.start, token.end);
            }
            partialTokens.clear();
        }
        collector.done();
        visitor.stoppedAt(stoppedAt, matchPoint);
    }

    public void forwardRemainder(MatchVisitor visitor) {
        if (stoppedAt == null) {
            throw new IllegalStateException("No stop position");
        }
        if (tokens != null) {
            for (Match token : tokens) {
                visitor.token(token.start, token.end);
            }
            tokens.clear();
        }
        if (partialTokens != null) {
            for (Match token : partialTokens) {
                visitor.token(token.start, token.end);
            }
            partialTokens.clear();
        }
        visitor.stoppedAt(stoppedAt, matchPoint);
    }

    private static class Match {
        final CharStream start;
        final CharStream end;

        Match(CharStream start, CharStream end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "\"" + start.upTo(end) + "\"";
        }
    }
}
