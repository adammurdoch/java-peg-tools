package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.ArrayList;
import java.util.List;

public class OptionalExpression extends AbstractExpression implements Expression, Matcher {
    private final Matcher matcher;

    public OptionalExpression(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public String toString() {
        return "{optional: " + matcher + "}";
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        CharStream pos = stream.tail();
        ArrayList<String> consumedTokens = new ArrayList<>();
        if (matcher.consume(pos, consumedTokens)) {
            stream.moveTo(pos);
            tokens.addAll(consumedTokens);
            return true;
        }
        return true;
    }
}
