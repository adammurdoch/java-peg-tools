package net.rubygrapefruit.parser.peg.internal.match;

import java.util.ArrayList;
import java.util.List;

public class BatchingMatchVisitor extends AbstractMatchVisitor implements ExpressionMatchResult {
    private List<TokenSource> results;

    @Override
    public void pushMatches(ResultCollector resultCollector) {
        if (results != null) {
            for (TokenSource result : results) {
                result.pushMatches(resultCollector);
            }
            results.clear();
        }
    }

    @Override
    protected void commit(TokenSource result) {
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(result);
    }

    @Override
    public TokenSource withBestAlternative() {
        return new TokenSource() {
            @Override
            public void pushMatches(ResultCollector resultCollector) {
                BatchingMatchVisitor.this.pushMatches(resultCollector);
                getBestAlternative().pushMatches(resultCollector);
            }
        };
    }
}
