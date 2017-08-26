package net.rubygrapefruit.parser.peg.internal;

/**
 * A stream of characters with a mutable position.
 */
public class CharStream {
    private final String input;
    private int pos = 0;

    public CharStream(String input) {
        this.input = input;
    }

    public CharStream(String input, int pos) {
        this.input = input;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return "{chars " + pos + " \"" + input.substring(pos) + "\"}";
    }

    /**
     * Returns a {@link CharStream} that contains the tail of this char stream. Changes made to the other stream are not visible through this stream.
     */
    public CharStream tail() {
        return new CharStream(input, pos);
    }

    public StreamPos current() {
        return new StreamPos(input, pos);
    }

    public void moveTo(StreamPos pos) {
        this.pos = pos.getOffset();
    }

    public void moveTo(CharStream stream) {
        pos = stream.pos;
    }

    /**
     * Consumes the given string, if it is at the start of the stream.
     *
     * @return true if consumed, false if not.
     */
    public boolean consume(String str) {
        if (input.regionMatches(pos, str, 0, str.length())) {
            pos += str.length();
            return true;
        }
        return false;
    }

    /**
     * Consumes a letter from the stream, if present.
     *
     * @return true if consumed, false if not.
     */
    public boolean consumeLetter() {
        if (pos >= input.length()) {
            return false;
        }
        char ch = input.charAt(pos);
        if (Character.isAlphabetic(ch)) {
            pos++;
            return true;
        }
        return false;
    }

    public boolean consumeOne() {
        if (pos >= input.length()) {
            return false;
        }
        pos++;
        return true;
    }

    public String diagnostic() {
        if (pos >= input.length()) {
            return "offset " + pos + ": end of input";
        }
        return "offset " + pos + ": [" + input.substring(pos, Math.min(input.length(), pos + 20)) + "]";
    }

    public boolean isAtEnd() {
        return pos >= input.length();
    }

    public StreamPos end() {
        return new StreamPos(input, input.length());
    }
}
