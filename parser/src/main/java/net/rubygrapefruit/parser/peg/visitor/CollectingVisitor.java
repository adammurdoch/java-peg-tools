package net.rubygrapefruit.parser.peg.visitor;

import net.rubygrapefruit.parser.peg.TokenVisitor;

import java.util.ArrayList;
import java.util.List;

public class CollectingVisitor implements TokenVisitor {
    private final List<String> result = new ArrayList<String>();

    public List<String> getResult() {
        return result;
    }

    @Override
    public void token(String token) {
        result.add(token);
    }

    @Override
    public void end() {
    }
}
