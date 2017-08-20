package net.rubygrapefruit.parser.peg.internal;

import java.util.Arrays;
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

    @Override
    public boolean isAcceptEmpty() {
        return matcher.isAcceptEmpty();
    }

    @Override
    public Set<? extends Terminal> getPrefixes() {
        return matcher.getPrefixes();
    }

    private static class SequenceMatcher implements Matcher, MatchExpression, MatchPoint {
        private final MatchExpression expression;
        private final SequenceMatcher next;

        SequenceMatcher(MatchExpression expression, SequenceMatcher next) {
            this.expression = expression;
            this.next = next;
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
            return expression.isAcceptEmpty() && next != null && next.isAcceptEmpty();
        }

        @Override
        public Set<? extends Terminal> getPrefixes() {
            if (!expression.isAcceptEmpty()) {
                return expression.getPrefixes();
            }

            Set<Terminal> prefixes = new HashSet<Terminal>();
            prefixes.addAll(expression.getPrefixes());
            if (next != null) {
                prefixes.addAll(next.getPrefixes());
            }
            return prefixes;
        }

        @Override
        public boolean consume(CharStream stream, MatchVisitor visitor) {
            BatchingMatchVisitor thisMatch = new BatchingMatchVisitor();
            CharStream pos = stream.tail();
            CharStream startThis = stream.tail();
            boolean matched = expression.getMatcher().consume(pos, thisMatch);
            stream.moveTo(pos);
            if (!matched) {
                thisMatch.forwardRemainder(expression.collector(visitor), visitor);
                return false;
            }
            if (next == null) {
                thisMatch.forwardAll(expression.collector(visitor), visitor);
                return true;
            }
            thisMatch.forwardMatches(expression.collector(visitor), visitor);
            boolean thisRecognizedSomething = thisMatch.getStoppedAt().diff(startThis) > 0;

            BatchingMatchVisitor nextMatch = new BatchingMatchVisitor();
            CharStream startNext = stream.tail();
            matched = next.consume(stream, nextMatch);
            boolean nextRecognizedSomething = nextMatch.getStoppedAt().diff(startNext) > 0;
            boolean nextRecognizedMore = nextMatch.getStoppedAt().diff(thisMatch.getStoppedAt()) >= 0;
            if (!matched) {
                if (thisRecognizedSomething && !nextRecognizedMore) {
                    // This recognized more than next, assume it is the best choice
                    thisMatch.forwardRemainder(expression.collector(visitor), visitor);
                } else if (!thisRecognizedSomething && !nextRecognizedSomething) {
                    // Neither recognized anything
                    visitor.stoppedAt(startNext, this);
                } else if (thisMatch.getMatchPoint() != null && thisMatch.getStoppedAt().diff(nextMatch.getStoppedAt()) == 0) {
                    // Recognized something, up to the same point
                    nextMatch.forwardRemainder(visitor);
                    visitor.stoppedAt(thisMatch.getStoppedAt(), new CompositeMatchPoint(Arrays.asList(thisMatch.getMatchPoint(), nextMatch.getMatchPoint())));
                } else {
                    // Assume the next is best choice
                    nextMatch.forwardRemainder(visitor);
                }
                return false;
            }
            int nextMatchedSomething = nextMatch.getMatchEnd().diff(startNext);
            if (nextMatchedSomething > 0) {
                // Next matched something, assume it is the best choice
                nextMatch.forwardAll(visitor);
                return true;
            }
            // next matched nothing
            if (nextRecognizedSomething && nextRecognizedMore) {
                // Next match nothing, recognized something plus recognised more than this, assume it is the best choice
                nextMatch.forwardAll(visitor);
                return true;
            }
            if (thisRecognizedSomething && !nextRecognizedMore) {
                // Next matched nothing, this recognised more than next, assume it is the best choice
                thisMatch.forwardAll(expression.collector(visitor), visitor);
                return true;
            }
            visitor.stoppedAt(startNext, this);
            return true;
        }
    }
}
