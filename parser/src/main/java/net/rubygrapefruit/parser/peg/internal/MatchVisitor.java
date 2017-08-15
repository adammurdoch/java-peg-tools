package net.rubygrapefruit.parser.peg.internal;

public interface MatchVisitor extends TokenCollector {
    void matched(CharStream endPos);

    void matched(CharStream endPos, CharStream stoppedAt);

    void failed(CharStream stoppedAt);
}
