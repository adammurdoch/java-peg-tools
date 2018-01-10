package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

public class MatchResult extends DefaultRegion {
    final Expression expression;

    public MatchResult(Expression expression, StreamPos start, StreamPos end) {
        super(start, end);
        this.expression = expression;
    }
}
