package net.rubygrapefruit.parser.peg.internal.match;

import java.util.Set;

public interface MatchPoint {
    Set<? extends Terminal> getPrefixes();
}
