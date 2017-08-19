package net.rubygrapefruit.parser.peg.internal;

import java.util.Set;

public interface MatchPoint {
    Set<? extends Terminal> getPrefixes();
}
