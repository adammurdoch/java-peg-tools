package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.ArrayList;
import java.util.List;

public class AnyOfExpression extends AbstractExpression implements Expression, Matcher {
    private final List<? extends Matcher> matchers;

    public AnyOfExpression(List<? extends Matcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public String toString() {
        return "{any-of " + matchers + "}";
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        for (Matcher matcher : matchers) {
            CharStream pos = stream.tail();
            ArrayList<String> maybeTokens = new ArrayList<>();
            if (matcher.consume(pos, maybeTokens)) {
                stream.moveTo(pos);
                tokens.addAll(maybeTokens);
                return true;
            }
        }
        return false;
    }
}
