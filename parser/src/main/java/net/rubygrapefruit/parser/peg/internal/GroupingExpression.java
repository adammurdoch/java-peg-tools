package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;

import java.util.List;

public class GroupingExpression implements Expression, MatchExpression {
    private final MatchExpression expression;

    GroupingExpression(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "{group: " + expression + "}";
    }

    @Override
    public Expression group() {
        return this;
    }

    @Override
    public Matcher getMatcher() {
        return expression.getMatcher();
    }

    @Override
    public void collectResult(List<String> tokens, MatchVisitor visitor) {
        if (tokens.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            builder.append(token);
        }
        visitor.token(builder.toString());
    }
}
