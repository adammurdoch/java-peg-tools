package net.rubygrapefruit.parser.peg.internal;

import net.rubygrapefruit.parser.peg.Region;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

public class DefaultRegion implements Region {
    final StreamPos start;
    final StreamPos end;

    protected DefaultRegion(StreamPos start, StreamPos end) {
        this.start = start;
        this.end = end;
    }

    public StreamPos getStart() {
        return start;
    }

    public StreamPos getEnd() {
        return end;
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
