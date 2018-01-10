package net.rubygrapefruit.parser.peg.internal.stream;

/**
 * An immutable position in a stream of characters.
 */
public interface StreamPos {
    /**
     * Returns the offset from the start of the stream, base 0.
     */
    int getOffset();

    /**
     * Base 1
     */
    int getLine();

    /**
     * Base 1
     */
    int getColumn();

    /**
     * Is this position the end of the stream?
     */
    boolean isAtEnd();

    /**
     * Returns the line containing this position, excluding the line ending.
     */
    String getCurrentLine();

    /**
     * Returns the text between this position and the given end position (exclusive).
     */
    String upTo(StreamPos end);

    /**
     * Returns the number of characters between this position and the other. Returns > 0 when this position is after the other, < 0 when this position is before the other.
     */
    int diff(StreamPos start);
}
