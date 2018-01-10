package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.ReferenceExpression;
import net.rubygrapefruit.parser.peg.internal.match.MatchExpression;
import net.rubygrapefruit.parser.peg.internal.match.Matcher;
import net.rubygrapefruit.parser.peg.internal.match.ResultCollector;
import net.rubygrapefruit.parser.peg.internal.match.TokenCollector;

public class DefaultReferenceExpression implements ReferenceExpression, MatchExpression {
    private boolean locked;
    private MatchExpression expression;

    @Override
    public String toString() {
        return "{ref: " + expression + "}";
    }

    @Override
    public void set(Expression expression) {
        synchronized (this) {
            if (locked) {
                throw new IllegalStateException("Cannot set the target for a reference expression after the reference has been used.");
            }
            this.expression = (MatchExpression) expression;
        }
    }

    private MatchExpression getAndLock() {
        synchronized (this) {
            if (!locked) {
                if (expression == null) {
                    throw new IllegalStateException("No target has been set for reference expression.");
                }
                locked = true;
            }
            return expression;
        }
    }

    @Override
    public ResultCollector collector(TokenCollector collector) {
        return getAndLock().collector(collector);
    }

    @Override
    public Matcher getMatcher() {
        return getAndLock().getMatcher();
    }

    @Override
    public Expression group() {
        return new GroupingExpression(this);
    }
}
