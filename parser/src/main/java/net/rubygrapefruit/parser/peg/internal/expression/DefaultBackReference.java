package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.BackReference;
import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

import java.util.Arrays;
import java.util.Set;

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

    private class Collector implements MatchExpression, Matcher {
        @Override
        public Set<? extends Terminal> getPrefixes() {
            return expression.getMatcher().getPrefixes();
        }

        @Override
        public boolean isAcceptEmpty() {
            return expression.getMatcher().isAcceptEmpty();
        }

        @Override
        public Matcher getMatcher() {
            return this;
        }

        @Override
        public boolean consume(CharStream stream, final MatchVisitor visitor) {
            CollectingVisitor collectingVisitor = new CollectingVisitor(stream.current(), visitor);
            boolean matched = expression.getMatcher().consume(stream, collectingVisitor);
            if (matched) {
                valueMatcher = new CharSequenceExpression(collectingVisitor.getText());
            }
            return matched;
        }
    }

    private static class CollectingVisitor implements MatchVisitor {
        private final MatchVisitor visitor;
        private final StreamPos start;
        private StreamPos end;

        CollectingVisitor(StreamPos start, MatchVisitor visitor) {
            this.start = start;
            this.visitor = visitor;
        }

        @Override
        public void attempted(ExpressionMatchResult result) {
            visitor.attempted(result);
        }

        @Override
        public void attempted(StreamPos pos, MatchPoint nextExpression) {
            visitor.attempted(pos, nextExpression);
        }

        @Override
        public void matched(StreamPos pos) {
            end = pos;
            visitor.matched(pos);
        }

        @Override
        public void matched(MatchResult result) {
            end = result.getEnd();
            visitor.matched(result);
        }

        @Override
        public void matched(ExpressionMatchResult result) {
            end = result.getMatchEnd();
            visitor.matched(result);
        }

        public String getText() {
            return start.upTo(end);
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
    }
}
