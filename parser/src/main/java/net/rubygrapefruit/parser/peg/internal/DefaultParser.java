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
            public void token(TextRegion token) {
                visitor.token(token);
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
            Set<String> candidates = new TreeSet<String>();
            for (Terminal terminal : resultVisitor.matchPoint.getPrefixes()) {
                candidates.add(terminal.getDisplayName());
            }
            if (!candidates.isEmpty()) {
                builder.append("\nexpected: ");
                boolean first = true;
                for (String candidate : candidates) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(", ");
                    }
                    builder.append(candidate);
                }
            }
            visitor.failed(builder.toString(), new TextRegion(pos, pos.end()));
        } else if (!pos.isAtEnd()) {
            visitor.failed("extra input at " + pos.diagnostic(), new TextRegion(pos, pos.end()));
        }
        return visitor;
    }

    private static class RootExpressionVisitor implements MatchVisitor {
        private final ResultCollector resultCollector;
        private CharStream matchEnd;
        private CharStream stoppedAt;
        private MatchPoint matchPoint;

        RootExpressionVisitor(ResultCollector resultCollector) {
            this.resultCollector = resultCollector;
        }

        @Override
        public void token(TextRegion token) {
            resultCollector.token(token);
        }

        @Override
        public void matched(CharStream endPos) {
            matchEnd = endPos;
            stoppedAt = endPos;
        }

        @Override
        public void stoppedAt(CharStream stoppedAt, MatchPoint matchPoint) {
            this.stoppedAt = stoppedAt;
            this.matchPoint = matchPoint;
        }
    }
}
