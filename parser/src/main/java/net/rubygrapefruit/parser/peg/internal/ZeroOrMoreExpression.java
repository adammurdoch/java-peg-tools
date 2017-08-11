package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.ArrayList;
import java.util.List;

public class ZeroOrMoreExpression extends AbstractExpression implements Expression, Matcher {
    private final Matcher matcher;

    public ZeroOrMoreExpression(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public String toString() {
        return "{zero-or-more: " + matcher + "}";
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        CharStream pos = stream.tail();
        ArrayList<String> maybeTokens = new ArrayList<>();
        while (true) {
            if (!matcher.consume(pos, maybeTokens)) {
                break;
            }
            stream.moveTo(pos);
            tokens.addAll(maybeTokens);
            maybeTokens.clear();
        }
        return true;
    }
}
