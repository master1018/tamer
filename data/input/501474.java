public final class Longs {
  private Longs() {}
  public static final int BYTES = Long.SIZE / Byte.SIZE;
  public static int hashCode(long value) {
    return (int) (value ^ (value >>> 32));
  }
  public static int compare(long a, long b) {
    return (a < b) ? -1 : ((a > b) ? 1 : 0);
  }
  public static boolean contains(long[] array, long target) {
    for (long value : array) {
      if (value == target) {
        return true;
      }
    }
    return false;
  }
  public static int indexOf(long[] array, long target) {
    return indexOf(array, target, 0, array.length);
  }
  private static int indexOf(
      long[] array, long target, int start, int end) {
    for (int i = start; i < end; i++) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }
  public static int indexOf(long[] array, long[] target) {
    checkNotNull(array, "array");
    checkNotNull(target, "target");
    if (target.length == 0) {
      return 0;
    }
    outer:
    for (int i = 0; i < array.length - target.length + 1; i++) {
      for (int j = 0; j < target.length; j++) {
        if (array[i + j] != target[j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }
  public static int lastIndexOf(long[] array, long target) {
    return lastIndexOf(array, target, 0, array.length);
  }
  private static int lastIndexOf(
      long[] array, long target, int start, int end) {
    for (int i = end - 1; i >= start; i--) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }
  public static long min(long... array) {
    checkArgument(array.length > 0);
    long min = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] < min) {
        min = array[i];
      }
    }
    return min;
  }
  public static long max(long... array) {
    checkArgument(array.length > 0);
    long max = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] > max) {
        max = array[i];
      }
    }
    return max;
  }
  public static long[] concat(long[]... arrays) {
    int length = 0;
    for (long[] array : arrays) {
      length += array.length;
    }
    long[] result = new long[length];
    int pos = 0;
    for (long[] array : arrays) {
      System.arraycopy(array, 0, result, pos, array.length);
      pos += array.length;
    }
    return result;
  }
  @GwtIncompatible("doesn't work")
  public static byte[] toByteArray(long value) {
    return new byte[] {
        (byte) (value >> 56),
        (byte) (value >> 48),
        (byte) (value >> 40),
        (byte) (value >> 32),
        (byte) (value >> 24),
        (byte) (value >> 16),
        (byte) (value >> 8),
        (byte) value};
  }
  @GwtIncompatible("doesn't work")
  public static long fromByteArray(byte[] bytes) {
    checkArgument(bytes.length >= BYTES,
        "array too small: %s < %s", bytes.length, BYTES);
    return (bytes[0] & 0xFFL) << 56
        | (bytes[1] & 0xFFL) << 48
        | (bytes[2] & 0xFFL) << 40
        | (bytes[3] & 0xFFL) << 32
        | (bytes[4] & 0xFFL) << 24
        | (bytes[5] & 0xFFL) << 16
        | (bytes[6] & 0xFFL) << 8
        | (bytes[7] & 0xFFL);
  }
  public static long[] ensureCapacity(
      long[] array, int minLength, int padding) {
    checkArgument(minLength >= 0, "Invalid minLength: %s", minLength);
    checkArgument(padding >= 0, "Invalid padding: %s", padding);
    return (array.length < minLength)
        ? copyOf(array, minLength + padding)
        : array;
  }
  private static long[] copyOf(long[] original, int length) {
    long[] copy = new long[length];
    System.arraycopy(original, 0, copy, 0, Math.min(original.length, length));
    return copy;
  }
  public static String join(String separator, long... array) {
    checkNotNull(separator);
    if (array.length == 0) {
      return "";
    }
    StringBuilder builder = new StringBuilder(array.length * 10);
    builder.append(array[0]);
    for (int i = 1; i < array.length; i++) {
      builder.append(separator).append(array[i]);
    }
    return builder.toString();
  }
  public static Comparator<long[]> lexicographicalComparator() {
    return LexicographicalComparator.INSTANCE;
  }
  private enum LexicographicalComparator implements Comparator<long[]> {
    INSTANCE;
    public int compare(long[] left, long[] right) {
      int minLength = Math.min(left.length, right.length);
      for (int i = 0; i < minLength; i++) {
        int result = Longs.compare(left[i], right[i]);
        if (result != 0) {
          return result;
        }
      }
      return left.length - right.length;
    }
  }
  public static long[] toArray(Collection<Long> collection) {
    if (collection instanceof LongArrayAsList) {
      return ((LongArrayAsList) collection).toLongArray();
    }
    Object[] boxedArray = collection.toArray();
    int len = boxedArray.length;
    long[] array = new long[len];
    for (int i = 0; i < len; i++) {
      array[i] = (Long) boxedArray[i];
    }
    return array;
  }
  public static List<Long> asList(long... backingArray) {
    if (backingArray.length == 0) {
      return Collections.emptyList();
    }
    return new LongArrayAsList(backingArray);
  }
  @GwtCompatible
  private static class LongArrayAsList extends AbstractList<Long>
      implements RandomAccess, Serializable {
    final long[] array;
    final int start;
    final int end;
    LongArrayAsList(long[] array) {
      this(array, 0, array.length);
    }
    LongArrayAsList(long[] array, int start, int end) {
      this.array = array;
      this.start = start;
      this.end = end;
    }
    @Override public int size() {
      return end - start;
    }
    @Override public boolean isEmpty() {
      return false;
    }
    @Override public Long get(int index) {
      checkElementIndex(index, size());
      return array[start + index];
    }
    @Override public boolean contains(Object target) {
      return (target instanceof Long)
          && Longs.indexOf(array, (Long) target, start, end) != -1;
    }
    @Override public int indexOf(Object target) {
      if (target instanceof Long) {
        int i = Longs.indexOf(array, (Long) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }
    @Override public int lastIndexOf(Object target) {
      if (target instanceof Long) {
        int i = Longs.lastIndexOf(array, (Long) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }
    @Override public Long set(int index, Long element) {
      checkElementIndex(index, size());
      long oldValue = array[start + index];
      array[start + index] = element;
      return oldValue;
    }
     public List<Long> subList(int fromIndex, int toIndex) {
      int size = size();
      checkPositionIndexes(fromIndex, toIndex, size);
      if (fromIndex == toIndex) {
        return Collections.emptyList();
      }
      return new LongArrayAsList(array, start + fromIndex, start + toIndex);
    }
    @Override public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof LongArrayAsList) {
        LongArrayAsList that = (LongArrayAsList) object;
        int size = size();
        if (that.size() != size) {
          return false;
        }
        for (int i = 0; i < size; i++) {
          if (array[start + i] != that.array[that.start + i]) {
            return false;
          }
        }
        return true;
      }
      return super.equals(object);
    }
    @Override public int hashCode() {
      int result = 1;
      for (int i = start; i < end; i++) {
        result = 31 * result + Longs.hashCode(array[i]);
      }
      return result;
    }
    @Override public String toString() {
      StringBuilder builder = new StringBuilder(size() * 10);
      builder.append('[').append(array[start]);
      for (int i = start + 1; i < end; i++) {
        builder.append(", ").append(array[i]);
      }
      return builder.append(']').toString();
    }
    long[] toLongArray() {
      int size = size();
      long[] result = new long[size];
      System.arraycopy(array, start, result, 0, size);
      return result;
    }
    private static final long serialVersionUID = 0;
  }
}
