package net.rubygrapefruit.parser.peg;

/**
 * Represents some region of the input, possibly empty.
 */
public interface Region {
    /**
     * Returns the text contained in this region.
     */
    String getText();
}
