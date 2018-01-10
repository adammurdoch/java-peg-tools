package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.Parser;
import net.rubygrapefruit.parser.peg.TokenVisitor;
import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

import java.util.Set;
import java.util.TreeSet;

public class DefaultParser implements Parser {
    private final MatchExpression rootExpression;

    public DefaultParser(MatchExpression rootExpression) {
        this.rootExpression = rootExpression;
    }

    @Override
    public <T extends TokenVisitor<Expression>> T parse(String input, final T visitor) {
        final TokenCollector resultCollector = new TokenCollector() {
            @Override
            public void token(MatchResult token) {
                visitor.token(token.getExpression(), token);
            }
        };
        RootExpressionVisitor resultVisitor = new RootExpressionVisitor(resultCollector);
        CharStream stream = new CharStream(input);
        boolean match = rootExpression.getMatcher().consume(stream, resultVisitor);
        resultVisitor.acceptBestAlternative();
        StreamPos pos = resultVisitor.getStoppedAt();
        // Did not recognize or did not match up to the end of input
        if (!match || !resultVisitor.getMatchEnd().isAtEnd()) {
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

    private static class RootExpressionVisitor extends AbstractMatchVisitor {
        private final TokenCollector resultCollector;

        RootExpressionVisitor(TokenCollector resultCollector) {
            this.resultCollector = resultCollector;
        }

        @Override
        protected void commit(TokenSource result) {
            result.pushMatches(resultCollector);
        }
    }
}
