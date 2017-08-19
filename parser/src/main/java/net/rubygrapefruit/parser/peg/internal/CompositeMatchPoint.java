package net.rubygrapefruit.parser.peg.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompositeMatchPoint implements MatchPoint {
    private final List<? extends MatchPoint> points;

    public CompositeMatchPoint(List<? extends MatchPoint> points) {
        this.points = points;
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
