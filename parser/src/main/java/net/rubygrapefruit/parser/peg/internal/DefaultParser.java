package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Parser;
import net.rubygrapefruit.parser.peg.TokenVisitor;

public class DefaultParser implements Parser {
    private final Matcher rootExpression;

    public DefaultParser(Matcher rootExpression) {
        this.rootExpression = rootExpression;
    }

    @Override
    public <T extends TokenVisitor> T parse(String input, final T visitor) {
        CharStream stream = new CharStream(input);
        rootExpression.consume(stream, new MatchVisitor() {
            @Override
            public void token(String token) {
                visitor.token(token);
            }
        });
        visitor.end();
        return visitor;
    }
}
