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
}
