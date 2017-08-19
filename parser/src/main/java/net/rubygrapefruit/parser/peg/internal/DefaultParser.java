package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Parser;
import net.rubygrapefruit.parser.peg.TokenVisitor;

import java.util.Set;
import java.util.TreeSet;

public class DefaultParser implements Parser {
    private final MatchExpression rootExpression;

    public DefaultParser(MatchExpression rootExpression) {
        this.rootExpression = rootExpression;
    }

    @Override
    public <T extends TokenVisitor> T parse(String input, final T visitor) {
        final ResultCollector resultCollector = rootExpression.collector(new TokenCollector() {
            @Override
            public void token(CharStream start, CharStream end) {
                visitor.token(start.upTo(end));
            }
        });
        RootExpressionVisitor resultVisitor = new RootExpressionVisitor(resultCollector);
        CharStream stream = new CharStream(input);
        boolean match = rootExpression.getMatcher().consume(stream, resultVisitor);
        resultCollector.done();
        CharStream pos = resultVisitor.stoppedAt;
        if (!match || pos.diff(resultVisitor.matchEnd) > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("stopped at ").append(pos.diagnostic());
            builder.append("\nexpected: ");
            Set<String> candidates = new TreeSet<String>();
            for (Terminal terminal : resultVisitor.nextExpression.getPrefixes()) {
                candidates.add(terminal.getDisplayName());
            }
            boolean first = true;
            for (String candidate : candidates) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append(candidate);
            }
            visitor.failed(builder.toString());
        } else if (!pos.isAtEnd()) {
            visitor.failed("extra input at " + pos.diagnostic());
        }
        return visitor;
    }

    private static class RootExpressionVisitor implements MatchVisitor {
        private final ResultCollector resultCollector;
        private CharStream matchEnd;
        private CharStream stoppedAt;
        private MatchExpression nextExpression;

        RootExpressionVisitor(ResultCollector resultCollector) {
            this.resultCollector = resultCollector;
        }

        @Override
        public void token(CharStream start, CharStream end) {
            resultCollector.token(start, end);
        }

        @Override
        public void matched(CharStream endPos) {
            matchEnd = endPos;
            stoppedAt = endPos;
        }

        @Override
        public void stoppedAt(CharStream stoppedAt, MatchExpression nextExpression) {
            this.stoppedAt = stoppedAt;
            this.nextExpression = nextExpression;
        }
    }
}
