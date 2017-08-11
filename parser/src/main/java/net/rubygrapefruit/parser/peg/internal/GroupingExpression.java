package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.ArrayList;
import java.util.List;

public class GroupingExpression implements Expression, Matcher {
    private final Matcher matcher;

    protected GroupingExpression(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public String toString() {
        return "{group: " + matcher + "}";
    }

    @Override
    public Expression group() {
        return this;
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        List<String> nested = new ArrayList<>();
        if (!matcher.consume(stream, nested)) {
            return false;
        }
        if (nested.isEmpty()) {
            return true;
        }
        StringBuilder builder = new StringBuilder();
        for (String token : nested) {
            builder.append(token);
        }
        tokens.add(builder.toString());
        return true;
    }
}
