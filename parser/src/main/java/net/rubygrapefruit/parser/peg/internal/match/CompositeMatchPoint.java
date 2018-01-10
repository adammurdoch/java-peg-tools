package net.rubygrapefruit.parser.peg.internal.match;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompositeMatchPoint implements MatchPoint {
    private final List<? extends MatchPoint> points;

    public CompositeMatchPoint(List<? extends MatchPoint> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (MatchPoint matchPoint : points) {
            for (Terminal terminal : matchPoint.getPrefixes()) {
                if (result.length() > 1) {
                    result.append(", ");
                }
                result.append(terminal.getDisplayName());
            }
        }
        result.append("]");
        return result.toString();
    }

    public static MatchPoint of(MatchPoint left, MatchPoint right) {
        if (left == null || left == NO_ALTERNATIVES) {
            return right;
        }
        if (right == null || right == NO_ALTERNATIVES) {
            return left;
        }
        return new CompositeMatchPoint(Arrays.asList(left, right));
    }

    @Override
    public Set<? extends Terminal> getPrefixes() {
        Set<Terminal> prefixes = new HashSet<Terminal>();
        for (MatchPoint matchPoint : points) {
            prefixes.addAll(matchPoint.getPrefixes());
        }
        return prefixes;
    }
}
