package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;
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
    public <T extends TokenVisitor<Expression>> T parse(String input, final T visitor) {
        final ResultCollector resultCollector = rootExpression.collector(new TokenCollector() {
            @Override
            public void token(MatchResult token) {
                visitor.token(token.expression, token);
            }
        });
        RootExpressionVisitor resultVisitor = new RootExpressionVisitor(resultCollector);
        CharStream stream = new CharStream(input);
        boolean match = rootExpression.getMatcher().consume(stream, resultVisitor);
        resultCollector.done();
        StreamPos pos = resultVisitor.stoppedAt;
        if (!match || pos.diff(resultVisitor.matchEnd) > 0) {
            StringBuilder builder = new StringBuilder();
            stream.moveTo(pos);
            builder.append("stopped at ").append(stream.diagnostic());
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
            visitor.failed(builder.toString(), new DefaultRegion(pos, stream.end()));
        } else if (!pos.isAtEnd()) {
            visitor.failed("extra input at " + stream.diagnostic(), new DefaultRegion(pos, stream.end()));
        }
        return visitor;
    }

    private static class RootExpressionVisitor implements MatchVisitor {
        private final ResultCollector resultCollector;
        private StreamPos matchEnd;
        private StreamPos stoppedAt;
        private MatchPoint matchPoint;

        RootExpressionVisitor(ResultCollector resultCollector) {
            this.resultCollector = resultCollector;
        }

        @Override
        public void token(MatchResult token) {
            resultCollector.token(token);
        }

        @Override
        public void matched(StreamPos endPos) {
            matchEnd = endPos;
            stoppedAt = endPos;
        }

        @Override
        public void stoppedAt(StreamPos stoppedAt, MatchPoint matchPoint) {
            this.stoppedAt = stoppedAt;
            this.matchPoint = matchPoint;
        }
    }
}
