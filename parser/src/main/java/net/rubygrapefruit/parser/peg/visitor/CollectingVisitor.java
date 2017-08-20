package net.rubygrapefruit.parser.peg.visitor;

import net.rubygrapefruit.parser.peg.Region;
import net.rubygrapefruit.parser.peg.TokenVisitor;

import java.util.ArrayList;
import java.util.List;

public class CollectingVisitor<T> implements TokenVisitor<T> {
    private final List<String> tokens = new ArrayList<String>();
    private final List<T> values = new ArrayList<T>();
    private String failure;

    public List<String> getTokens() {
        return tokens;
    }

    public List<T> getValues() {
        return values;
    }

    public String getFailure() {
        return failure;
    }

    @Override
    public void token(T value, Region match) {
        values.add(value);
        tokens.add(match.getText());
    }

    @Override
    public void failed(String message, Region remainder) {
        this.failure = message;
    }
}
