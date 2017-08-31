package net.rubygrapefruit.parser.peg.internal;

/**
 * A stream of characters with a mutable read position.
 */
public class CharStream {
    private final String input;
    private int startLine = 0;
    private int line = 1;
    private int pos = 0;
    private DefaultStreamPos end;

    public CharStream(String input) {
        this.input = input;
    }

    private CharStream(String input, int pos, int startLine, int line) {
        this.input = input;
        this.pos = pos;
        this.startLine = startLine;
        this.line = line;
    }

    @Override
    public String toString() {
        return "{chars " + pos + " \"" + input.substring(pos) + "\"}";
    }

    /**
     * Returns a {@link CharStream} that contains the tail of this char stream. Changes made to the other stream are not visible through this stream
     * and vice versa.
     */
    public CharStream tail() {
        return new CharStream(input, pos, startLine, line);
    }

    public StreamPos current() {
        return new DefaultStreamPos(input, pos, startLine, line);
    }

    public void moveTo(StreamPos pos) {
        DefaultStreamPos p = (DefaultStreamPos) pos;
        this.pos = p.pos;
        this.startLine = p.startLine;
        this.line = p.line;
    }

    public void moveTo(CharStream stream) {
        pos = stream.pos;
        startLine = stream.startLine;
        line = stream.line;
    }

    /**
     * Consumes the given string, if it is at the start of the stream.
     *
     * @return true if consumed, false if not.
     */
    public boolean consume(String str) {
        if (input.regionMatches(pos, str, 0, str.length())) {
            moveForward(str.length());
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
            moveForward(1);
            return true;
        }
        return false;
    }

    /**
     * Consumed a single character, if any remain.
     *
     * @return true if consumed, false if not.
     */
    public boolean consumeOne() {
        if (pos >= input.length()) {
            return false;
        }
        moveForward(1);
        return true;
    }

    private void moveForward(int count) {
        for (int i = 0; i < count; i++) {
            char ch = input.charAt(pos);
            if (ch == '\n') {
                line++;
                startLine = pos + 1;
            }
            pos++;
        }
    }

    public boolean isAtEnd() {
        return pos >= input.length();
    }

    public StreamPos end() {
        if (end == null) {
            int line = this.line;
            int startLine = this.startLine;
            for (int i = pos; i < input.length(); i++) {
                if (input.charAt(i) == '\n') {
                    line++;
                    startLine = i + 1;
                }
            }
            end = new DefaultStreamPos(input, input.length(), startLine, line);
        }
        return end;
    }

    private static class DefaultStreamPos implements StreamPos {
        private final String input;
        private int pos = 0;
        private final int startLine;
        private final int line;

        public DefaultStreamPos(String input, int pos, int startLine, int line) {
            this.input = input;
            this.pos = pos;
            this.startLine = startLine;
            this.line = line;
        }

        /**
         * Returns the offset from the start of the stream.
         */
        public int getOffset() {
            return pos;
        }

        /**
         * Base 1
         */
        public int getLine() {
            return line;
        }

        /**
         * Base 1
         */
        public int getColumn() {
            return pos - startLine + 1;
        }

        public boolean isAtEnd() {
            return pos >= input.length();
        }

        @Override
        public String getCurrentLine() {
            int endLine = pos;
            while (endLine < input.length() && input.charAt(endLine) != '\n') {
                endLine++;
            }
            if (endLine < input.length() && endLine > 0 && input.charAt(endLine - 1) == '\r') {
                endLine--;
            }
            return input.substring(startLine, endLine);
        }

        /**
         * Returns the text between this position and the given end position (exclusive).
         */
        public String upTo(StreamPos end) {
            return input.substring(pos, end.getOffset());
        }

        /**
         * Returns the number of characters between this position and the other. Returns > 0 when this position is after the other, < 0 when this
         * position is before the other.
         */
        public int diff(StreamPos start) {
            return pos - start.getOffset();
        }
    }
}
