package net.rubygrapefruit.parser.peg.internal.expressions;

import net.rubygrapefruit.parser.peg.BackReference;
import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.internal.*;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

import java.util.Arrays;

public class DefaultBackReference implements BackReference {
    private final MatchExpression expression;
    private final ValueExpression valueExpression;
    private MatchExpression valueMatcher;

    public DefaultBackReference(MatchExpression expression) {
        this.expression = expression;
        valueExpression = new ValueExpression();
    }

    @Override
    public String toString() {
        return "{back-reference to: " + expression + "}";
    }

    @Override
    public Expression getValue() {
        return valueExpression;
    }

    @Override
    public Expression followedBy(Expression expression) {
        return new SequenceExpression(Arrays.asList(new Collector(), (MatchExpression) expression));
    }

    private class Collector implements MatchExpression {
        @Override
        public Matcher getMatcher() {
            return expression.getMatcher();
        }

        @Override
        public ResultCollector collector(TokenCollector collector) {
            final ResultCollector delegate = expression.collector(collector);
            return new ResultCollector() {
                private StreamPos startValue;
                private StreamPos endValue;
                @Override
                public void token(MatchResult token) {
                    if (startValue == null) {
                        startValue = token.getStart();
                    }
                    endValue = token.getEnd();
                    delegate.token(token);
                }

                @Override
                public void done() {
                    if (startValue != null) {
                        valueMatcher = new CharSequenceExpression(startValue.upTo(endValue));
                    }
                    delegate.done();
                }
            };
        }
    }

    private class ValueExpression implements Expression, MatchExpression {
        @Override
        public String toString() {
            return "{value-of " + expression + "}";
        }

        @Override
        public Expression group() {
            return new GroupingExpression(this);
        }

        @Override
        public Matcher getMatcher() {
            return valueMatcher.getMatcher();
        }

        @Override
        public ResultCollector collector(final TokenCollector collector) {
            return new ResultCollector() {
                private StreamPos startValue;
                private StreamPos endValue;
                @Override
                public void token(MatchResult token) {
                    if (startValue == null) {
                        startValue = token.getStart();
                    }
                    endValue = token.getEnd();
                }

                @Override
                public void done() {
                    if (startValue != null) {
                        collector.token(new MatchResult(valueExpression, startValue, endValue));
                        startValue = null;
                        endValue = null;
                    }
                }
            };
        }
    }
}
