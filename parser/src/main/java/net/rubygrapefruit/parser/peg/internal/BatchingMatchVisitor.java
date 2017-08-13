package net.rubygrapefruit.parser.peg.internal;

import java.util.ArrayList;
import java.util.List;

public class BatchingMatchVisitor implements MatchVisitor {
    private List<Match> tokens;
    private CharStream stoppedAt;

    @Override
    public void token(CharStream start, CharStream end) {
        if (tokens == null) {
            tokens = new ArrayList<Match>();
        }
        tokens.add(new Match(start, end));
    }

    public CharStream getStoppedAt() {
        return stoppedAt;
    }

    @Override
    public void stoppedAt(CharStream pos) {
        stoppedAt = pos;
    }

    /**
     * Forwards the result collected by this visitor.
     */
    public void forward(ResultCollector visitor) {
        if (tokens != null) {
            for (Match token : tokens) {
                visitor.token(token.start, token.end);
            }
            visitor.done();
            tokens.clear();
        }
    }

    public void reset() {
        if (tokens != null) {
            tokens.clear();
        }
        stoppedAt = null;
    }

    public int matches() {
        if (tokens == null) {
            return 0;
        }
        return tokens.size();
    }

    private static class Match {
        final CharStream start;
        final CharStream end;

        Match(CharStream start, CharStream end) {
            this.start = start;
            this.end = end;
        }
    }
}
