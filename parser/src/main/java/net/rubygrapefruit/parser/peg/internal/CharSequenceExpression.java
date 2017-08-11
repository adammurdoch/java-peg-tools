package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class CharSequenceExpression implements Expression, Matcher {
    private final String str;

    public CharSequenceExpression(String str) {
        this.str = str;
    }

    @Override
    public boolean consume(CharStream stream, List<String> tokens) {
        if (stream.consume(str)) {
            tokens.add(str);
            return true;
        }
        return false;
    }
}
