package net.rubygrapefruit.parser.peg.internal.match;

import java.util.Collections;
import java.util.Set;

public interface MatchPoint {
    Set<? extends Terminal> getPrefixes();

    MatchPoint NO_ALTERNATIVES = new MatchPoint() {
        @Override
        public Set<? extends Terminal> getPrefixes() {
            return Collections.emptySet();
        }

        @Override
        public String toString() {
            return "[]";
        }
    };
}
