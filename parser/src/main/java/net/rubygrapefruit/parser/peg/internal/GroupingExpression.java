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
    public BufferingMatchVisitor collector(final MatchVisitor visitor) {
        return new TokenMergingMatchVisitor(visitor);
    }

    private static class TokenMergingMatchVisitor implements BufferingMatchVisitor {
        private final MatchVisitor visitor;
        StringBuilder builder;

        public TokenMergingMatchVisitor(MatchVisitor visitor) {
            this.visitor = visitor;
            builder = new StringBuilder();
        }

        @Override
        public void token(String token) {
            builder.append(token);
        }

        @Override
        public void done() {
            if (builder.length() > 0) {
                visitor.token(builder.toString());
            }
        }
    }
}
