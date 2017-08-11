package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Parser;
import net.rubygrapefruit.parser.peg.TokenVisitor;

import java.util.ArrayList;
import java.util.List;

public class DefaultParser implements Parser {
    private final Matcher rootExpression;

    public DefaultParser(Matcher rootExpression) {
        this.rootExpression = rootExpression;
    }

    @Override
    public <T extends TokenVisitor> T parse(String input, T visitor) {
        CharStream stream = new CharStream(input);
        List<String> result = new ArrayList<>();
        rootExpression.consume(stream, result);
        for (String token : result) {
            visitor.token(token);
        }
        visitor.end();
        return visitor;
    }
}
