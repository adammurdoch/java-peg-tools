package net.rubygrapefruit.parser.peg.internal;

/**
 * An immutable position in a stream of characters.
 */
public class StreamPos {
    private final String input;
    private int pos = 0;

    public StreamPos(String input, int pos) {
        this.input = input;
        this.pos = pos;
    }

    public int getOffset() {
        return pos;
    }

    public boolean isAtEnd() {
        return pos >= input.length();
    }

    public String upTo(StreamPos end) {
        return input.substring(pos, end.pos);
    }

    public int diff(StreamPos start) {
        return pos - start.pos;
    }
}
