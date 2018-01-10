package net.rubygrapefruit.parser.peg.internal.expression;

import net.rubygrapefruit.parser.peg.Expression;
import net.rubygrapefruit.parser.peg.internal.match.*;
import net.rubygrapefruit.parser.peg.internal.stream.CharStream;
import net.rubygrapefruit.parser.peg.internal.stream.StreamPos;

import java.util.Set;

public class GroupingExpression implements Expression, MatchExpression, Matcher {
    private final MatchExpression expression;

    GroupingExpression(MatchExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "{group: " + expression + "}";
    }

    @Override
    public Expression group() {
        return this;
    }

    @Override
    public Matcher getMatcher() {
        return this;
    }

    @Override
    public Set<? extends Terminal> getPrefixes() {
        return expression.getMatcher().getPrefixes();
    }

    @Override
    public boolean isAcceptEmpty() {
        return expression.getMatcher().isAcceptEmpty();
    }

    @Override
    public boolean consume(CharStream stream, MatchVisitor visitor) {
        GroupingVisitor groupingVisitor = new GroupingVisitor(stream.current(), visitor, this);
        boolean match = expression.getMatcher().consume(stream, groupingVisitor);
        groupingVisitor.done(match);
        return match;
    }

    private static class GroupingVisitor implements MatchVisitor {
        final StreamPos start;
        final MatchVisitor visitor;
        final GroupingExpression expression;
        StreamPos end;
        StreamPos stoppedAt;
        MatchPoint matchPoint = MatchPoint.NO_ALTERNATIVES;

        GroupingVisitor(StreamPos start, MatchVisitor visitor, GroupingExpression expression) {
            this.start = start;
            this.end = start;
            this.visitor = visitor;
            this.expression = expression;
        }

        @Override
        public void attempted(ExpressionMatchResult result) {
            attempted(result.getStoppedAt(), result.getMatchPoint());
        }

        @Override
        public void attempted(StreamPos pos, MatchPoint nextExpression) {
            if (stoppedAt == null) {
                stoppedAt = pos;
                matchPoint = nextExpression;
                return;
            }

            int diff = pos.diff(stoppedAt);
            if (diff > 0) {
                stoppedAt = pos;
                matchPoint = nextExpression;
            } else if (diff == 0) {
                matchPoint = CompositeMatchPoint.of(matchPoint, nextExpression);
            }
        }

        @Override
        public void matched(StreamPos pos) {
            matched(pos, MatchPoint.NO_ALTERNATIVES);
        }

        @Override
        public void matched(MatchResult result) {
            matched(result.getEnd(), MatchPoint.NO_ALTERNATIVES);
        }

        @Override
        public void matched(ExpressionMatchResult result) {
            matched(result.getMatchEnd(), result.getMatchPoint());
        }

        private void matched(StreamPos matchedTo, MatchPoint matchPoint) {
            end = matchedTo;
            if (stoppedAt == null) {
                stoppedAt = matchedTo;
                this.matchPoint = matchPoint;
                return;
            }
            int diff = matchedTo.diff(stoppedAt);
            if (diff > 0) {
                stoppedAt = matchedTo;
                this.matchPoint = matchPoint;
            } else if (diff == 0) {
                this.matchPoint = CompositeMatchPoint.of(this.matchPoint, matchPoint);
            }
        }

        void done(boolean matched) {
            if (matched) {
                visitor.matched(new ExpressionMatchResult() {
                    @Override
                    public TokenSource withBestAlternative() {
                        if (stoppedAt.diff(start) == 0) {
                            return EMPTY;
                        }
                        return new TokenSource() {
                            @Override
                            public void pushMatches(TokenCollector resultCollector) {
                                resultCollector.token(new MatchResult(expression, start, stoppedAt));
                            }
                        };
                    }

                    @Override
                    public boolean hasPartialMatches() {
                        return stoppedAt.diff(end) > 0;
                    }

                    @Override
                    public StreamPos getMatchEnd() {
                        return end;
                    }

                    @Override
                    public StreamPos getStoppedAt() {
                        return stoppedAt;
                    }

                    @Override
                    public MatchPoint getMatchPoint() {
                        return matchPoint;
                    }

                    @Override
                    public void pushMatches(TokenCollector resultCollector) {
                        if (end.diff(start) > 0) {
                            resultCollector.token(new MatchResult(expression, start, end));
                        }
                    }
                });
            } else {
                visitor.attempted(new ExpressionMatchResult() {
                    @Override
                    public TokenSource withBestAlternative() {
                        if (stoppedAt.diff(start) == 0) {
                            return EMPTY;
                        }
                        return new TokenSource() {
                            @Override
                            public void pushMatches(TokenCollector resultCollector) {
                                resultCollector.token(new MatchResult(expression, start, stoppedAt));
                            }
                        };
                    }

                    @Override
                    public boolean hasPartialMatches() {
                        return stoppedAt.diff(start) > 0;
                    }

                    @Override
                    public StreamPos getMatchEnd() {
                        return end;
                    }

                    @Override
                    public StreamPos getStoppedAt() {
                        return stoppedAt;
                    }

                    @Override
                    public MatchPoint getMatchPoint() {
                        return matchPoint;
                    }

                    @Override
                    public void pushMatches(TokenCollector resultCollector) {
                    }
                });
            }
        }
    }
}
