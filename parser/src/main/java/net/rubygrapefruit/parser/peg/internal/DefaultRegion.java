package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Region;

class DefaultRegion implements Region {
    final CharStream start;
    final CharStream end;

    DefaultRegion(CharStream start, CharStream end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String getText() {
        return start.upTo(end);
    }

    @Override
    public String toString() {
        return "\"" + getText() + "\"";
    }
}
