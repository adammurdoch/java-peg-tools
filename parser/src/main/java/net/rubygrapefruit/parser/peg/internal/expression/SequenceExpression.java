package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;

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
                visitor.attempted(thisMatch);
                return false;
            }
            visitor.matched(thisMatch);
            if (next == null) {
                return true;
            }

            BatchingMatchVisitor nextMatch = new BatchingMatchVisitor();
            matched = next.consume(stream, nextMatch);
            if (!matched) {
                visitor.attempted(nextMatch);
                return false;
            }
            visitor.matched(nextMatch);
            return true;
        }
    }
}
