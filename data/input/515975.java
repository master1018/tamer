public final class Splitter {
  private final CharMatcher trimmer;
  private final boolean omitEmptyStrings;
  private final Strategy strategy;
  private Splitter(Strategy strategy) {
    this(strategy, false, CharMatcher.NONE);
  }
  private Splitter(Strategy strategy, boolean omitEmptyStrings,
      CharMatcher trimmer) {
    this.strategy = strategy;
    this.omitEmptyStrings = omitEmptyStrings;
    this.trimmer = trimmer;
  }
  public static Splitter on(char separator) {
    return on(CharMatcher.is(separator));
  }
  public static Splitter on(final CharMatcher separatorMatcher) {
    checkNotNull(separatorMatcher);
    return new Splitter(new Strategy() {
       public SplittingIterator iterator(
          Splitter splitter, final CharSequence toSplit) {
        return new SplittingIterator(splitter, toSplit) {
          @Override int separatorStart(int start) {
            return separatorMatcher.indexIn(toSplit, start);
          }
          @Override int separatorEnd(int separatorPosition) {
            return separatorPosition + 1;
          }
        };
      }
    });
  }
  public static Splitter on(final String separator) {
    checkArgument(separator.length() != 0,
        "The separator may not be the empty string.");
    return new Splitter(new Strategy() {
       public SplittingIterator iterator(
          Splitter splitter, CharSequence toSplit) {
        return new SplittingIterator(splitter, toSplit) {
          @Override public int separatorStart(int start) {
            int delimeterLength = separator.length();
            positions:
            for (int p = start, last = toSplit.length() - delimeterLength;
                p <= last; p++) {
              for (int i = 0; i < delimeterLength; i++) {
                if (toSplit.charAt(i + p) != separator.charAt(i)) {
                  continue positions;
                }
              }
              return p;
            }
            return -1;
          }
          @Override public int separatorEnd(int separatorPosition) {
            return separatorPosition + separator.length();
          }
        };
      }
    });
  }
  public static Splitter on(final Pattern separatorPattern) {
    checkNotNull(separatorPattern);
    checkArgument(!separatorPattern.matcher("").matches(),
        "The pattern may not match the empty string: %s", separatorPattern);
    return new Splitter(new Strategy() {
       public SplittingIterator iterator(
          final Splitter splitter, CharSequence toSplit) {
        final Matcher matcher = separatorPattern.matcher(toSplit);
        return new SplittingIterator(splitter, toSplit) {
          @Override public int separatorStart(int start) {
            return matcher.find(start) ? matcher.start() : -1;
          }
          @Override public int separatorEnd(int separatorPosition) {
            return matcher.end();
          }
        };
      }
    });
  }
  public static Splitter onPattern(String separatorPattern) {
    return on(Pattern.compile(separatorPattern));
  }
  public static Splitter fixedLength(final int length) {
    checkArgument(length > 0, "The length may not be less than 1");
    return new Splitter(new Strategy() {
       public SplittingIterator iterator(
          final Splitter splitter, CharSequence toSplit) {
        return new SplittingIterator(splitter, toSplit) {
          @Override public int separatorStart(int start) {
            int nextChunkStart = start + length;
            return (nextChunkStart < toSplit.length() ? nextChunkStart : -1);
          }
          @Override public int separatorEnd(int separatorPosition) {
            return separatorPosition;
          }
        };
      }
    });
  }
  public Splitter omitEmptyStrings() {
    return new Splitter(strategy, true, trimmer);
  }
  public Splitter trimResults() {
    return trimResults(CharMatcher.WHITESPACE);
  }
  public Splitter trimResults(CharMatcher trimmer) {
    checkNotNull(trimmer);
    return new Splitter(strategy, omitEmptyStrings, trimmer);
  }
  public Iterable<String> split(final CharSequence sequence) {
    checkNotNull(sequence);
    return new Iterable<String>() {
       public Iterator<String> iterator() {
        return strategy.iterator(Splitter.this, sequence);
      }
    };
  }
  private interface Strategy {
    Iterator<String> iterator(Splitter splitter, CharSequence toSplit);
  }
  private abstract static class SplittingIterator
      extends AbstractIterator<String> {
    final CharSequence toSplit;
    final CharMatcher trimmer;
    final boolean omitEmptyStrings;
    abstract int separatorStart(int start);
    abstract int separatorEnd(int separatorPosition);
    int offset = 0;
    protected SplittingIterator(Splitter splitter, CharSequence toSplit) {
      this.trimmer = splitter.trimmer;
      this.omitEmptyStrings = splitter.omitEmptyStrings;
      this.toSplit = toSplit;
    }
    @Override protected String computeNext() {
      while (offset != -1) {
        int start = offset;
        int end;
        int separatorPosition = separatorStart(offset);
        if (separatorPosition == -1) {
          end = toSplit.length();
          offset = -1;
        } else {
          end = separatorPosition;
          offset = separatorEnd(separatorPosition);
        }
        while (start < end && trimmer.matches(toSplit.charAt(start))) {
          start++;
        }
        while (end > start && trimmer.matches(toSplit.charAt(end - 1))) {
          end--;
        }
        if (omitEmptyStrings && start == end) {
          continue;
        }
        return toSplit.subSequence(start, end).toString();
      }
      return endOfData();
    }
  }
  private static abstract class AbstractIterator<T> implements Iterator<T> {
    State state = State.NOT_READY;
    enum State {
      READY, NOT_READY, DONE, FAILED,
    }
    T next;
    protected abstract T computeNext();
    protected final T endOfData() {
      state = State.DONE;
      return null;
    }
    public final boolean hasNext() {
      checkState(state != State.FAILED);
      switch (state) {
        case DONE:
          return false;
        case READY:
          return true;
        default:
      }
      return tryToComputeNext();
    }
    boolean tryToComputeNext() {
      state = State.FAILED; 
      next = computeNext();
      if (state != State.DONE) {
        state = State.READY;
        return true;
      }
      return false;
    }
    public final T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      state = State.NOT_READY;
      return next;
    }
     public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
