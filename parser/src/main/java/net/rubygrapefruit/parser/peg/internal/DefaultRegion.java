package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Region;

class DefaultRegion implements Region {
    final StreamPos start;
    final StreamPos end;

    DefaultRegion(StreamPos start, StreamPos end) {
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
