package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.Parser;
import net.rubygrapefruit.parser.peg.TokenVisitor;
import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class DefaultParser implements Parser {
    private static final NoAlternatives NO_ALTERNATIVES = new NoAlternatives();
    private final MatchExpression rootExpression;

    public DefaultParser(MatchExpression rootExpression) {
        this.rootExpression = rootExpression;
    }

    @Override
    public <T extends TokenVisitor<Expression>> T parse(String input, final T visitor) {
        final ResultCollector resultCollector = rootExpression.collector(new TokenCollector() {
            @Override
            public void token(MatchResult token) {
                visitor.token(token.getExpression(), token);
            }
        });
        RootExpressionVisitor resultVisitor = new RootExpressionVisitor(resultCollector);
        CharStream stream = new CharStream(input);
        boolean match = rootExpression.getMatcher().consume(stream, resultVisitor);
        resultCollector.done();
        StreamPos pos = resultVisitor.stoppedAt;
        // Did not recognize or did not match up to the end of input
        if (!match || !resultVisitor.matchEnd.isAtEnd()) {
            StringBuilder builder = new StringBuilder();
            builder.append("line ").append(pos.getLine()).append(":");
            Set<String> candidates = new TreeSet<String>();
            for (Terminal terminal : resultVisitor.getMatchPoint().getPrefixes()) {
                candidates.add(terminal.getDisplayName());
            }
            if (!candidates.isEmpty()) {
                builder.append(" expected ");
                int count = 0;
                for (String candidate : candidates) {
                    if (count > 0 && count == candidates.size() - 1) {
                        builder.append(" or ");
                    } else if (count > 0) {
                        builder.append(", ");
                    }
                    count++;
                    builder.append(candidate);
                }
            } else {
                builder.append(" unexpected characters");
            }
            appendHighlight(pos, builder);
            visitor.failed(builder.toString(), new DefaultRegion(pos, stream.end()));
        }
        return visitor;
    }

    private void appendHighlight(StreamPos pos, StringBuilder builder) {
        builder.append('\n');
        builder.append(pos.getCurrentLine());
        builder.append('\n');
        for (int i = 1; i < pos.getColumn(); i++) {
            builder.append(' ');
        }
        builder.append('^');
    }

    private static class RootExpressionVisitor implements MatchVisitor {
        private final ResultCollector resultCollector;
        private StreamPos matchEnd;
        private StreamPos stoppedAt;
        private MatchPoint matchPoint;

        RootExpressionVisitor(ResultCollector resultCollector) {
            this.resultCollector = resultCollector;
        }

        public MatchPoint getMatchPoint() {
            return matchPoint == null ?  NO_ALTERNATIVES : matchPoint;
        }

        @Override
        public void matched(StreamPos endPos) {
            matchEnd = endPos;
            stoppedAt = endPos;
            matchPoint = null;
        }

        @Override
        public void matched(MatchResult result) {
            resultCollector.token(result);
            matchEnd = result.getEnd();
            stoppedAt = matchEnd;
            matchPoint = null;
        }

        @Override
        public void matched(ExpressionMatchResult result) {
            result.pushTokens(resultCollector);
            matchEnd = result.getMatchEnd();
            stoppedAt = result.getStoppedAt();
            matchPoint = result.getMatchPoint();
        }

        @Override
        public void attempted(StreamPos pos, MatchPoint nextExpression) {
            if (stoppedAt == null || pos.diff(stoppedAt) > 0) {
                stoppedAt = pos;
                matchPoint = nextExpression;
            }
        }

        @Override
        public void attempted(ExpressionMatchResult result) {
            attempted(result.getStoppedAt(), result.getMatchPoint());
        }
    }

    private static class NoAlternatives implements MatchPoint {
        @Override
        public Set<? extends Terminal> getPrefixes() {
            return Collections.emptySet();
        }
    }
}
