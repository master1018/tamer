public final class Chars {
  private Chars() {}
  public static final int BYTES = Character.SIZE / Byte.SIZE;
  public static int hashCode(char value) {
    return value;
  }
  public static char checkedCast(long value) {
    char result = (char) value;
    checkArgument(result == value, "Out of range: %s", value);
    return result;
  }
  public static char saturatedCast(long value) {
    if (value > Character.MAX_VALUE) {
      return Character.MAX_VALUE;
    }
    if (value < Character.MIN_VALUE) {
      return Character.MIN_VALUE;
    }
    return (char) value;
  }
  public static int compare(char a, char b) {
    return a - b; 
  }
  public static boolean contains(char[] array, char target) {
    for (char value : array) {
      if (value == target) {
        return true;
      }
    }
    return false;
  }
  public static int indexOf(char[] array, char target) {
    return indexOf(array, target, 0, array.length);
  }
  private static int indexOf(
      char[] array, char target, int start, int end) {
    for (int i = start; i < end; i++) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }
  public static int indexOf(char[] array, char[] target) {
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
  public static int lastIndexOf(char[] array, char target) {
    return lastIndexOf(array, target, 0, array.length);
  }
  private static int lastIndexOf(
      char[] array, char target, int start, int end) {
    for (int i = end - 1; i >= start; i--) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }
  public static char min(char... array) {
    checkArgument(array.length > 0);
    char min = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] < min) {
        min = array[i];
      }
    }
    return min;
  }
  public static char max(char... array) {
    checkArgument(array.length > 0);
    char max = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] > max) {
        max = array[i];
      }
    }
    return max;
  }
  public static char[] concat(char[]... arrays) {
    int length = 0;
    for (char[] array : arrays) {
      length += array.length;
    }
    char[] result = new char[length];
    int pos = 0;
    for (char[] array : arrays) {
      System.arraycopy(array, 0, result, pos, array.length);
      pos += array.length;
    }
    return result;
  }
  @GwtIncompatible("doesn't work")
  public static byte[] toByteArray(char value) {
    return new byte[] {
        (byte) (value >> 8),
        (byte) value};
  }
  @GwtIncompatible("doesn't work")
  public static char fromByteArray(byte[] bytes) {
    checkArgument(bytes.length >= BYTES,
        "array too small: %s < %s", bytes.length, BYTES);
    return (char) ((bytes[0] << 8) | (bytes[1] & 0xFF));
  }
  public static char[] ensureCapacity(
      char[] array, int minLength, int padding) {
    checkArgument(minLength >= 0, "Invalid minLength: %s", minLength);
    checkArgument(padding >= 0, "Invalid padding: %s", padding);
    return (array.length < minLength)
        ? copyOf(array, minLength + padding)
        : array;
  }
  private static char[] copyOf(char[] original, int length) {
    char[] copy = new char[length];
    System.arraycopy(original, 0, copy, 0, Math.min(original.length, length));
    return copy;
  }
  public static String join(String separator, char... array) {
    checkNotNull(separator);
    int len = array.length;
    if (len == 0) {
      return "";
    }
    StringBuilder builder
        = new StringBuilder(len + separator.length() * (len - 1));
    builder.append(array[0]);
    for (int i = 1; i < len; i++) {
      builder.append(separator).append(array[i]);
    }
    return builder.toString();
  }
  public static Comparator<char[]> lexicographicalComparator() {
    return LexicographicalComparator.INSTANCE;
  }
  private enum LexicographicalComparator implements Comparator<char[]> {
    INSTANCE;
    public int compare(char[] left, char[] right) {
      int minLength = Math.min(left.length, right.length);
      for (int i = 0; i < minLength; i++) {
        int result = Chars.compare(left[i], right[i]);
        if (result != 0) {
          return result;
        }
      }
      return left.length - right.length;
    }
  }
  public static char[] toArray(Collection<Character> collection) {
    if (collection instanceof CharArrayAsList) {
      return ((CharArrayAsList) collection).toCharArray();
    }
    Object[] boxedArray = collection.toArray();
    int len = boxedArray.length;
    char[] array = new char[len];
    for (int i = 0; i < len; i++) {
      array[i] = (Character) boxedArray[i];
    }
    return array;
  }
  public static List<Character> asList(char... backingArray) {
    if (backingArray.length == 0) {
      return Collections.emptyList();
    }
    return new CharArrayAsList(backingArray);
  }
  @GwtCompatible
  private static class CharArrayAsList extends AbstractList<Character>
      implements RandomAccess, Serializable {
    final char[] array;
    final int start;
    final int end;
    CharArrayAsList(char[] array) {
      this(array, 0, array.length);
    }
    CharArrayAsList(char[] array, int start, int end) {
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
    @Override public Character get(int index) {
      checkElementIndex(index, size());
      return array[start + index];
    }
    @Override public boolean contains(Object target) {
      return (target instanceof Character)
          && Chars.indexOf(array, (Character) target, start, end) != -1;
    }
    @Override public int indexOf(Object target) {
      if (target instanceof Character) {
        int i = Chars.indexOf(array, (Character) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }
    @Override public int lastIndexOf(Object target) {
      if (target instanceof Character) {
        int i = Chars.lastIndexOf(array, (Character) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }
    @Override public Character set(int index, Character element) {
      checkElementIndex(index, size());
      char oldValue = array[start + index];
      array[start + index] = element;
      return oldValue;
    }
     public List<Character> subList(int fromIndex, int toIndex) {
      int size = size();
      checkPositionIndexes(fromIndex, toIndex, size);
      if (fromIndex == toIndex) {
        return Collections.emptyList();
      }
      return new CharArrayAsList(array, start + fromIndex, start + toIndex);
    }
    @Override public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof CharArrayAsList) {
        CharArrayAsList that = (CharArrayAsList) object;
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
        result = 31 * result + Chars.hashCode(array[i]);
      }
      return result;
    }
    @Override public String toString() {
      StringBuilder builder = new StringBuilder(size() * 3);
      builder.append('[').append(array[start]);
      for (int i = start + 1; i < end; i++) {
        builder.append(", ").append(array[i]);
      }
      return builder.append(']').toString();
    }
    char[] toCharArray() {
      int size = size();
      char[] result = new char[size];
      System.arraycopy(array, start, result, 0, size);
      return result;
    }
    private static final long serialVersionUID = 0;
  }
}
