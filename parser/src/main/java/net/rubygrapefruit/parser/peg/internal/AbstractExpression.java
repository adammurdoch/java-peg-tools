package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public abstract class AbstractExpression implements Expression, MatchExpression {
    @Override
    public Expression group() {
        return new GroupingExpression(this);
    }

    @Override
    public void collectResult(List<String> tokens, MatchVisitor visitor) {
        for (String token : tokens) {
            visitor.token(token);
        }
    }
}
