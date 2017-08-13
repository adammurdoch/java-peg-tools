package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

public abstract class AbstractExpression implements Expression, MatchExpression {
    @Override
    public Expression group() {
        return new GroupingExpression(this);
    }

    @Override
    public BufferingMatchVisitor collector(final MatchVisitor visitor) {
        return new ForwardingVisitor(visitor);
    }

    private static class ForwardingVisitor implements BufferingMatchVisitor {
        private final MatchVisitor visitor;

        public ForwardingVisitor(MatchVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void done() {
        }

        @Override
        public void token(String token) {
            visitor.token(token);
        }
    }
}
