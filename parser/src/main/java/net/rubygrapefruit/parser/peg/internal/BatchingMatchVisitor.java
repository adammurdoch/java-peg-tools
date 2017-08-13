package net.rubygrapefruit.parser.peg.internal;

import java.util.ArrayList;
import java.util.List;

public class BatchingMatchVisitor implements MatchVisitor {
    private List<String> tokens;
    private CharStream stoppedAt;

    @Override
    public void token(String token) {
        if (tokens == null) {
            tokens = new ArrayList<String>();
        }
        tokens.add(token);
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
            for (String token : tokens) {
                visitor.token(token);
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
}
