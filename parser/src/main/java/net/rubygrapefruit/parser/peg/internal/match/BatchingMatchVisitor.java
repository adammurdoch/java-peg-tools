package net.rubygrapefruit.parser.peg.internal.match;

import java.util.ArrayList;
import java.util.List;

public class BatchingMatchVisitor extends AbstractMatchVisitor implements ExpressionMatchResult {
    private List<ExpressionMatchResult> results;

    @Override
    public void pushMatches(ResultCollector resultCollector) {
        if (results != null) {
            for (ExpressionMatchResult result : results) {
                result.pushMatches(resultCollector);
            }
            results.clear();
        }
    }

    @Override
    protected void addResult(ExpressionMatchResult result) {
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(result);
    }

    @Override
    public void pushAll(ResultCollector resultCollector) {
        pushMatches(resultCollector);
        super.pushAll(resultCollector);
    }
}
