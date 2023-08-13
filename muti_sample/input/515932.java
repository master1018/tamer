@GwtCompatible public class Joiner {
  public static Joiner on(String separator) {
    return new Joiner(separator);
  }
  public static Joiner on(char separator) {
    return new Joiner(String.valueOf(separator));
  }
  private final String separator;
  private Joiner(String separator) {
    this.separator = checkNotNull(separator);
  }
  private Joiner(Joiner prototype) {
    this.separator = prototype.separator;
  }
  public <A extends Appendable> A appendTo(A appendable, Iterable<?> parts)
      throws IOException {
    checkNotNull(appendable);
    Iterator<?> iterator = parts.iterator();
    if (iterator.hasNext()) {
      appendable.append(toString(iterator.next()));
      while (iterator.hasNext()) {
        appendable.append(separator);
        appendable.append(toString(iterator.next()));
      }
    }
    return appendable;
  }
  public final <A extends Appendable> A appendTo(
      A appendable, Object[] parts) throws IOException {
    return appendTo(appendable, Arrays.asList(parts));
  }
  public final <A extends Appendable> A appendTo(A appendable,
      @Nullable Object first, @Nullable Object second, Object... rest)
      throws IOException {
    return appendTo(appendable, iterable(first, second, rest));
  }
  public final StringBuilder appendTo(StringBuilder builder, Iterable<?> parts)
  {
    try {
      appendTo((Appendable) builder, parts);
    } catch (IOException impossible) {
      throw new AssertionError(impossible);
    }
    return builder;
  }
  public final StringBuilder appendTo(StringBuilder builder, Object[] parts) {
    return appendTo(builder, Arrays.asList(parts));
  }
  public final StringBuilder appendTo(StringBuilder builder,
      @Nullable Object first, @Nullable Object second, Object... rest) {
    return appendTo(builder, iterable(first, second, rest));
  }
  public final String join(Iterable<?> parts) {
    return appendTo(new StringBuilder(), parts).toString();
  }
  public final String join(Object[] parts) {
    return join(Arrays.asList(parts));
  }
  public final String join(
      @Nullable Object first, @Nullable Object second, Object... rest) {
    return join(iterable(first, second, rest));
  }
  public Joiner useForNull(final String nullText) {
    checkNotNull(nullText);
    return new Joiner(this) {
      @Override CharSequence toString(Object part) {
        return (part == null) ? nullText : Joiner.this.toString(part);
      }
      @Override public Joiner useForNull(String nullText) {
        checkNotNull(nullText); 
        throw new UnsupportedOperationException("already specified useForNull");
      }
      @Override public Joiner skipNulls() {
        throw new UnsupportedOperationException("already specified useForNull");
      }
    };
  }
  public Joiner skipNulls() {
    return new Joiner(this) {
      @Override public <A extends Appendable> A appendTo(
          A appendable, Iterable<?> parts) throws IOException {
        checkNotNull(appendable, "appendable");
        checkNotNull(parts, "parts");
        Iterator<?> iterator = parts.iterator();
        while (iterator.hasNext()) {
          Object part = iterator.next();
          if (part != null) {
            appendable.append(Joiner.this.toString(part));
            break;
          }
        }
        while (iterator.hasNext()) {
          Object part = iterator.next();
          if (part != null) {
            appendable.append(separator);
            appendable.append(Joiner.this.toString(part));
          }
        }
        return appendable;
      }
      @Override public Joiner useForNull(String nullText) {
        checkNotNull(nullText); 
        throw new UnsupportedOperationException("already specified skipNulls");
      }
      @Override public MapJoiner withKeyValueSeparator(String kvs) {
        checkNotNull(kvs); 
        throw new UnsupportedOperationException(
            "can't use .skipNulls() with maps");
      }
    };
  }
  public MapJoiner withKeyValueSeparator(String keyValueSeparator) {
    return new MapJoiner(this, checkNotNull(keyValueSeparator));
  }
  public static class MapJoiner {
    private Joiner joiner;
    private String keyValueSeparator;
    private MapJoiner(Joiner joiner, String keyValueSeparator) {
      this.joiner = joiner;
      this.keyValueSeparator = keyValueSeparator;
    }
    public <A extends Appendable> A appendTo(A appendable, Map<?, ?> map)
        throws IOException {
      checkNotNull(appendable);
      Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
      if (iterator.hasNext()) {
        Entry<?, ?> entry = iterator.next();
        appendable.append(joiner.toString(entry.getKey()));
        appendable.append(keyValueSeparator);
        appendable.append(joiner.toString(entry.getValue()));
        while (iterator.hasNext()) {
          appendable.append(joiner.separator);
          Entry<?, ?> e = iterator.next();
          appendable.append(joiner.toString(e.getKey()));
          appendable.append(keyValueSeparator);
          appendable.append(joiner.toString(e.getValue()));
        }
      }
      return appendable;
    }
    public StringBuilder appendTo(StringBuilder builder, Map<?, ?> map) {
      try {
        appendTo((Appendable) builder, map);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
      return builder;
    }
    public String join(Map<?, ?> map) {
      return appendTo(new StringBuilder(), map).toString();
    }
    public MapJoiner useForNull(String nullText) {
      return new MapJoiner(joiner.useForNull(nullText), keyValueSeparator);
    }
  }
  CharSequence toString(Object part) {
    return (part instanceof CharSequence)
        ? (CharSequence) part
        : part.toString();
  }
  private static Iterable<Object> iterable(
      final Object first, final Object second, final Object[] rest) {
    checkNotNull(rest);
    return new AbstractList<Object>() {
      @Override public int size() {
        return rest.length + 2;
      }
      @Override public Object get(int index) {
        switch (index) {
          case 0:
            return first;
          case 1:
            return second;
          default:
            return rest[index - 2];
        }
      }
    };
  }
}
