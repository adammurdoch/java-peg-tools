package net.rubygrapefruit.parser.peg.internal;

public class CharStream {
    private final String input;
    private int pos = 0;

    public CharStream(String input) {
        this.input = input;
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
     * Consumes up to the given string, possibly none. Does not consume the given string.
     * @return The consumed chars, or null if none.
     */
    public String consumeUpTo(String delim) {
        int end = input.indexOf(delim, pos);
        if (end >= 0 && end > pos) {
            String token = input.substring(pos, end);
            pos = end;
            return token;
        }
        return null;
    }
}
