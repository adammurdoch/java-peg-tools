package net.rubygrapefruit.parser.peg.internal;

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
        return "{chars: \"" + input.substring(pos) + "\"}";
    }

    /**
     * Returns a {@link CharStream} that contains the tail of this char stream. Changes made to the other stream are not visible.
     */
    public CharStream tail() {
        return new CharStream(input, pos);
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
     * Consumes up to the given delimit or the end of input. Does not consume the given string.
     * @return The consumed chars, or null if none.
     */
    public String consumeLetter() {
        if (pos >= input.length()) {
            return null;
        }
        char ch = input.charAt(pos);
        if (Character.isAlphabetic(ch)) {
            pos++;
            return Character.toString(ch);
        }
        return null;
    }

    public String diagnostic() {
        if (pos >= input.length()) {
            return "end of input";
        }
        return "offset " + pos + " '" + input.substring(pos, Math.min(input.length(), pos + 20)) + "'";
    }

    public boolean isAtEnd() {
        return pos >= input.length();
    }

    public String upTo(CharStream end) {
        return input.substring(pos, end.pos);
    }
}
