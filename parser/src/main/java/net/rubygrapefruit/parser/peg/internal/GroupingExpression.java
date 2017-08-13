package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

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
        return new TokenMergingMatchVisitor(collector);
    }

    private static class TokenMergingMatchVisitor implements ResultCollector {
        private final TokenCollector collector;
        StringBuilder builder;

        TokenMergingMatchVisitor(TokenCollector collector) {
            this.collector = collector;
        }

        @Override
        public void token(String token) {
            if (builder == null) {
                builder = new StringBuilder();
            }
            builder.append(token);
        }

        @Override
        public void done() {
            if (builder != null && builder.length() > 0) {
                collector.token(builder.toString());
            }
        }
    }
}
