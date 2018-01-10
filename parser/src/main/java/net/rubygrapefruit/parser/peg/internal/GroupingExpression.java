package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

public class GroupingExpression implements Expression, MatchExpression {
    private final MatchExpression expression;

    GroupingExpression(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "{group: " + expression + "}";
    }

    @Override
    public Expression group() {
        return this;
    }

    @Override
    public Matcher getMatcher() {
        return expression.getMatcher();
    }

    @Override
    public ResultCollector collector(TokenCollector collector) {
        return new TokenMergingMatchVisitor(this, collector);
    }

    private static class TokenMergingMatchVisitor implements ResultCollector {
        private GroupingExpression expression;
        private final TokenCollector collector;
        private StreamPos start;
        private StreamPos end;

        TokenMergingMatchVisitor(GroupingExpression expression, TokenCollector collector) {
            this.expression = expression;
            this.collector = collector;
        }

        @Override
        public void token(MatchResult token) {
            if (this.start == null) {
                this.start = token.start;
            }
            this.end = token.end;
        }

        @Override
        public void done() {
            if (start != null) {
                collector.token(new MatchResult(expression, start, end));
                start = null;
            }
        }
    }
}
