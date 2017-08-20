package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Region;

class TextRegion implements Region {
    final CharStream start;
    final CharStream end;

    TextRegion(CharStream start, CharStream end) {
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
