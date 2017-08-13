package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Parser;
import net.rubygrapefruit.parser.peg.TokenVisitor;

public class DefaultParser implements Parser {
    private final MatchExpression rootExpression;

    public DefaultParser(MatchExpression rootExpression) {
        this.rootExpression = rootExpression;
    }

    @Override
    public <T extends TokenVisitor> T parse(String input, final T visitor) {
        CharStream stream = new CharStream(input);
        final ResultCollector resultCollector = rootExpression.collector(new TokenCollector() {
            @Override
            public void token(String token) {
                visitor.token(token);
            }
        });
        RootExpressionVisitor resultVisitor = new RootExpressionVisitor(resultCollector);
        boolean match = rootExpression.getMatcher().consume(stream, resultVisitor);
        resultCollector.done();
        CharStream pos = resultVisitor.stoppedAt;
        if (!match) {
            visitor.failed("stopped at: " + pos.diagnostic());
        } else if (!pos.isAtEnd()) {
            visitor.failed("extra input: " + pos.diagnostic());
        }
        return visitor;
    }

    private static class RootExpressionVisitor implements MatchVisitor {
        private final ResultCollector resultCollector;
        private CharStream stoppedAt;

        RootExpressionVisitor(ResultCollector resultCollector) {
            this.resultCollector = resultCollector;
        }

        @Override
        public void token(String token) {
            resultCollector.token(token);
        }

        @Override
        public void stoppedAt(CharStream pos) {
            stoppedAt = pos;
        }
    }
}
