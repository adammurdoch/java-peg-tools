package net.rubygrapefruit.parser.peg.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SequenceExpression extends AbstractExpression {
    private final List<? extends MatchExpression> expressions;
    private final SequenceMatcher matcher;

    public SequenceExpression(List<? extends MatchExpression> expressions) {
        this.expressions = expressions;
        SequenceMatcher matcher = null;
        for (int i = expressions.size() - 1; i >= 0; i--) {
            matcher = new SequenceMatcher(expressions.get(i), matcher);
        }
        this.matcher = matcher;
    }

    @Override
    public String toString() {
        return "{sequence: " + expressions + "}";
    }

    @Override
    public Matcher getMatcher() {
        return matcher;
    }

    private static class SequenceMatcher implements Matcher, MatchExpression, MatchPoint {
        private final MatchExpression expression;
        private final SequenceMatcher next;

        SequenceMatcher(MatchExpression expression, SequenceMatcher next) {
            this.expression = expression;
            this.next = next;
        }

        @Override
        public String toString() {
            if (next == null) {
                return expression.toString();
            }
            return expression + " " + next;
        }

        @Override
        public ResultCollector collector(TokenCollector collector) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Matcher getMatcher() {
            return this;
        }

        @Override
        public boolean isAcceptEmpty() {
            return expression.getMatcher().isAcceptEmpty() && next != null && next.isAcceptEmpty();
        }

        @Override
        public Set<? extends Terminal> getPrefixes() {
            if (!expression.getMatcher().isAcceptEmpty()) {
                return expression.getMatcher().getPrefixes();
            }

            Set<Terminal> prefixes = new HashSet<Terminal>();
            prefixes.addAll(expression.getMatcher().getPrefixes());
            if (next != null) {
                prefixes.addAll(next.getPrefixes());
            }
            return prefixes;
        }

        @Override
        public boolean consume(CharStream stream, MatchVisitor visitor) {
            BatchingMatchVisitor thisMatch = new BatchingMatchVisitor();
            CharStream tail = stream.tail();
            boolean matched = expression.getMatcher().consume(tail, thisMatch);
            stream.moveTo(tail);
            if (!matched) {
                thisMatch.forwardRemainder(expression.collector(visitor), visitor);
                return false;
            }
            if (next == null) {
                thisMatch.forwardAll(expression.collector(visitor), visitor);
                return true;
            }
            thisMatch.forwardMatches(expression.collector(visitor), visitor);

            BatchingMatchVisitor nextMatch = new BatchingMatchVisitor();
            matched = next.consume(stream, nextMatch);
            int diff = nextMatch.getStoppedAt().diff(thisMatch.getStoppedAt());
            if (!matched) {
                if (diff < 0) {
                    // This recognized more than next, assume it is the best choice
                    thisMatch.forwardRemainder(expression.collector(visitor), visitor);
                } else if (diff == 0) {
                    // Recognized up to the same point
                    nextMatch.forwardRemainder(visitor);
                    visitor.stoppedAt(thisMatch.getStoppedAt(), CompositeMatchPoint.of(thisMatch.getMatchPoint(), nextMatch.getMatchPoint()));
                } else {
                    // Assume the next is best choice
                    nextMatch.forwardRemainder(visitor);
                }
                return false;
            }
            nextMatch.forwardMatches(visitor);

            if (diff < 0) {
                // This recognized more than next
                visitor.stoppedAt(thisMatch.getStoppedAt(), thisMatch.getMatchPoint());
            } else if (diff == 0) {
                // Recognized up to the same point
                nextMatch.forwardRemainder(visitor);
                visitor.stoppedAt(thisMatch.getStoppedAt(), CompositeMatchPoint.of(thisMatch.getMatchPoint(), nextMatch.getMatchPoint()));
            } else {
                // Assume the next is best choice
                nextMatch.forwardRemainder(visitor);
            }
            return true;
        }
    }
}
