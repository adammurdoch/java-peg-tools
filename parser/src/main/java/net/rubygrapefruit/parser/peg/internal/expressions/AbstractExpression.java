package net.rubygrapefruit.parser.peg.internal.expressions;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.internal.MatchExpression;
import net.rubygrapefruit.parser.peg.internal.MatchResult;
import net.rubygrapefruit.parser.peg.internal.ResultCollector;
import net.rubygrapefruit.parser.peg.internal.TokenCollector;

public abstract class AbstractExpression implements Expression, MatchExpression {
    @Override
    public Expression group() {
        return new GroupingExpression(this);
    }

    @Override
    public ResultCollector collector(final TokenCollector collector) {
        return new ForwardingVisitor(collector);
    }

    private static class ForwardingVisitor implements ResultCollector {
        private final TokenCollector collector;

        public ForwardingVisitor(TokenCollector collector) {
            this.collector = collector;
        }

        @Override
        public void done() {
        }

        @Override
        public void token(MatchResult token) {
            collector.token(token);
        }
    }
}
