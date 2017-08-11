package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Parser;

import java.util.ArrayList;
import java.util.List;

public class DefaultParser implements Parser {
    private final Matcher rootExpression;

    public DefaultParser(Matcher rootExpression) {
        this.rootExpression = rootExpression;
    }

    @Override
    public List<String> parse(String input) {
        CharStream stream = new CharStream(input);
        ArrayList<String> result = new ArrayList<>();
        rootExpression.consume(stream, result);
        return result;
    }
}
