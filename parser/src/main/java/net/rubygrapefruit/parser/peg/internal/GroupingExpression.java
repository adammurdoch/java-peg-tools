package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

public class GroupingExpression implements Expression, MatchExpression, Matcher {
    private final Matcher matcher;

    GroupingExpression(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public String toString() {
        return "{group: " + matcher + "}";
    }

    @Override
    public Expression group() {
        return this;
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        MergingMatchVisitor nested = new MergingMatchVisitor();
        if (!matcher.consume(stream, nested)) {
            return false;
        }
        nested.forward(visitor);
        return true;
    }

    private static class MergingMatchVisitor implements MatchVisitor {
        StringBuilder builder;

        @Override
        public void token(String token) {
            if (builder == null) {
                builder = new StringBuilder();
            }
            builder.append(token);
        }

        void forward(MatchVisitor visitor) {
            if (builder == null || builder.length() == 0) {
                return;
            }
            visitor.token(builder.toString());
        }
    }
}
