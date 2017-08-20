package net.rubygrapefruit.parser.peg.visitor;

import net.rubygrapefruit.parser.peg.Region;
import net.rubygrapefruit.parser.peg.TokenVisitor;

import java.util.ArrayList;
import java.util.List;

public class CollectingVisitor implements TokenVisitor {
    private final List<String> result = new ArrayList<String>();
    private String failure;

    public List<String> getResult() {
        return result;
    }

    public String getFailure() {
        return failure;
    }

    @Override
    public void token(Region match) {
        result.add(match.getText());
    }

    @Override
    public void failed(String message, Region remainder) {
        this.failure = message;
    }
}
