package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.ArrayList;
import java.util.List;

public class SequenceExpression extends AbstractExpression implements Expression, Matcher {
    private final List<Matcher> matchers;

    public SequenceExpression(List<Matcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public String toString() {
        return "{sequence: " + matchers + "}";
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        CharStream pos = stream.tail();
        List<String> nested = new ArrayList<>();
        for (Matcher matcher : matchers) {
            if (!matcher.consume(pos, nested)) {
                return false;
            }
        }
        stream.moveTo(pos);
        tokens.addAll(nested);
        return true;
    }
}
