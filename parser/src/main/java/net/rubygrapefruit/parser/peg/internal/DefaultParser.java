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
        BufferingMatchVisitor matchVisitor = rootExpression.collector(new MatchVisitor() {
            @Override
            public void token(String token) {
                visitor.token(token);
            }
        });
        boolean match = rootExpression.getMatcher().consume(stream, matchVisitor);
        matchVisitor.done();
        if (!match) {
            visitor.failed("stopped at: " + stream.diagnostic());
        } else if (!stream.isAtEnd()) {
            visitor.failed("extra input: " + stream.diagnostic());
        }
        return visitor;
    }
}
