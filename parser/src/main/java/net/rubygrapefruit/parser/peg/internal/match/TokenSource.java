package net.rubygrapefruit.parser.peg.internal.match;

public interface TokenSource {
    TokenSource EMPTY = new TokenSource() {
        @Override
        public void pushMatches(TokenCollector resultCollector) {
        }
    };

    /**
     * Push the matches from this result to the given collector.
     */
    void pushMatches(TokenCollector resultCollector);
}
