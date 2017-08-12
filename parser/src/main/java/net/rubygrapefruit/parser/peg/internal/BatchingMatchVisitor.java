package net.rubygrapefruit.parser.peg.internal;

import java.util.ArrayList;
import java.util.List;

public class BatchingMatchVisitor implements MatchVisitor {
    private List<String> tokens;

    @Override
    public void token(String token) {
        if (tokens == null) {
            tokens = new ArrayList<String>();
        }
        tokens.add(token);
    }

    public void forward(MatchVisitor visitor) {
        if (tokens != null) {
            for (String token : tokens) {
                visitor.token(token);
            }
            tokens.clear();
        }
    }

    public void reset() {
        if (tokens != null) {
            tokens.clear();
        }
    }

    public int matches() {
        if (tokens == null) {
            return 0;
        }
        return tokens.size();
    }
}
