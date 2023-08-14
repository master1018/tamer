public final class Shorts {
  private Shorts() {}
  public static final int BYTES = Short.SIZE / Byte.SIZE;
  public static int hashCode(short value) {
    return value;
  }
  public static short checkedCast(long value) {
    short result = (short) value;
    checkArgument(result == value, "Out of range: %s", value);
    return result;
  }
  public static short saturatedCast(long value) {
    if (value > Short.MAX_VALUE) {
      return Short.MAX_VALUE;
    }
    if (value < Short.MIN_VALUE) {
      return Short.MIN_VALUE;
    }
    return (short) value;
  }
  public static int compare(short a, short b) {
    return a - b; 
  }
  public static boolean contains(short[] array, short target) {
    for (short value : array) {
      if (value == target) {
        return true;
      }
    }
    return false;
  }
  public static int indexOf(short[] array, short target) {
    return indexOf(array, target, 0, array.length);
  }
  private static int indexOf(
      short[] array, short target, int start, int end) {
    for (int i = start; i < end; i++) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }
  public static int indexOf(short[] array, short[] target) {
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
  public static int lastIndexOf(short[] array, short target) {
    return lastIndexOf(array, target, 0, array.length);
  }
  private static int lastIndexOf(
      short[] array, short target, int start, int end) {
    for (int i = end - 1; i >= start; i--) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }
  public static short min(short... array) {
    checkArgument(array.length > 0);
    short min = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] < min) {
        min = array[i];
      }
    }
    return min;
  }
  public static short max(short... array) {
    checkArgument(array.length > 0);
    short max = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] > max) {
        max = array[i];
      }
    }
    return max;
  }
  public static short[] concat(short[]... arrays) {
    int length = 0;
    for (short[] array : arrays) {
      length += array.length;
    }
    short[] result = new short[length];
    int pos = 0;
    for (short[] array : arrays) {
      System.arraycopy(array, 0, result, pos, array.length);
      pos += array.length;
    }
    return result;
  }
  @GwtIncompatible("doesn't work")
  public static byte[] toByteArray(short value) {
    return new byte[] {
        (byte) (value >> 8),
        (byte) value};
  }
  @GwtIncompatible("doesn't work")
  public static short fromByteArray(byte[] bytes) {
    checkArgument(bytes.length >= BYTES,
        "array too small: %s < %s", bytes.length, BYTES);
    return (short) ((bytes[0] << 8) | (bytes[1] & 0xFF));
  }
  public static short[] ensureCapacity(
      short[] array, int minLength, int padding) {
    checkArgument(minLength >= 0, "Invalid minLength: %s", minLength);
    checkArgument(padding >= 0, "Invalid padding: %s", padding);
    return (array.length < minLength)
        ? copyOf(array, minLength + padding)
        : array;
  }
  private static short[] copyOf(short[] original, int length) {
    short[] copy = new short[length];
    System.arraycopy(original, 0, copy, 0, Math.min(original.length, length));
    return copy;
  }
  public static String join(String separator, short... array) {
    checkNotNull(separator);
    if (array.length == 0) {
      return "";
    }
    StringBuilder builder = new StringBuilder(array.length * 6);
    builder.append(array[0]);
    for (int i = 1; i < array.length; i++) {
      builder.append(separator).append(array[i]);
    }
    return builder.toString();
  }
  public static Comparator<short[]> lexicographicalComparator() {
    return LexicographicalComparator.INSTANCE;
  }
  private enum LexicographicalComparator implements Comparator<short[]> {
    INSTANCE;
    public int compare(short[] left, short[] right) {
      int minLength = Math.min(left.length, right.length);
      for (int i = 0; i < minLength; i++) {
        int result = Shorts.compare(left[i], right[i]);
        if (result != 0) {
          return result;
        }
      }
      return left.length - right.length;
    }
  }
  public static short[] toArray(Collection<Short> collection) {
    if (collection instanceof ShortArrayAsList) {
      return ((ShortArrayAsList) collection).toShortArray();
    }
    Object[] boxedArray = collection.toArray();
    int len = boxedArray.length;
    short[] array = new short[len];
    for (int i = 0; i < len; i++) {
      array[i] = (Short) boxedArray[i];
    }
    return array;
  }
  public static List<Short> asList(short... backingArray) {
    if (backingArray.length == 0) {
      return Collections.emptyList();
    }
    return new ShortArrayAsList(backingArray);
  }
  @GwtCompatible
  private static class ShortArrayAsList extends AbstractList<Short>
      implements RandomAccess, Serializable {
    final short[] array;
    final int start;
    final int end;
    ShortArrayAsList(short[] array) {
      this(array, 0, array.length);
    }
    ShortArrayAsList(short[] array, int start, int end) {
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
    @Override public Short get(int index) {
      checkElementIndex(index, size());
      return array[start + index];
    }
    @Override public boolean contains(Object target) {
      return (target instanceof Short)
          && Shorts.indexOf(array, (Short) target, start, end) != -1;
    }
    @Override public int indexOf(Object target) {
      if (target instanceof Short) {
        int i = Shorts.indexOf(array, (Short) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }
    @Override public int lastIndexOf(Object target) {
      if (target instanceof Short) {
        int i = Shorts.lastIndexOf(array, (Short) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }
    @Override public Short set(int index, Short element) {
      checkElementIndex(index, size());
      short oldValue = array[start + index];
      array[start + index] = element;
      return oldValue;
    }
     public List<Short> subList(int fromIndex, int toIndex) {
      int size = size();
      checkPositionIndexes(fromIndex, toIndex, size);
      if (fromIndex == toIndex) {
        return Collections.emptyList();
      }
      return new ShortArrayAsList(array, start + fromIndex, start + toIndex);
    }
    @Override public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof ShortArrayAsList) {
        ShortArrayAsList that = (ShortArrayAsList) object;
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
        result = 31 * result + Shorts.hashCode(array[i]);
      }
      return result;
    }
    @Override public String toString() {
      StringBuilder builder = new StringBuilder(size() * 6);
      builder.append('[').append(array[start]);
      for (int i = start + 1; i < end; i++) {
        builder.append(", ").append(array[i]);
      }
      return builder.append(']').toString();
    }
    short[] toShortArray() {
      int size = size();
      short[] result = new short[size];
      System.arraycopy(array, start, result, 0, size);
      return result;
    }
    private static final long serialVersionUID = 0;
  }
}
