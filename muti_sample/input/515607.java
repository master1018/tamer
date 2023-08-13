public final class Floats {
  private Floats() {}
  public static int hashCode(float value) {
    return ((Float) value).hashCode();
  }
  public static int compare(float a, float b) {
    return Float.compare(a, b);
  }
  public static boolean contains(float[] array, float target) {
    for (float value : array) {
      if (value == target) {
        return true;
      }
    }
    return false;
  }
  public static int indexOf(float[] array, float target) {
    return indexOf(array, target, 0, array.length);
  }
  private static int indexOf(
      float[] array, float target, int start, int end) {
    for (int i = start; i < end; i++) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }
  public static int indexOf(float[] array, float[] target) {
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
  public static int lastIndexOf(float[] array, float target) {
    return lastIndexOf(array, target, 0, array.length);
  }
  private static int lastIndexOf(
      float[] array, float target, int start, int end) {
    for (int i = end - 1; i >= start; i--) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }
  public static float min(float... array) {
    checkArgument(array.length > 0);
    float min = array[0];
    for (int i = 1; i < array.length; i++) {
      min = Math.min(min, array[i]);
    }
    return min;
  }
  public static float max(float... array) {
    checkArgument(array.length > 0);
    float max = array[0];
    for (int i = 1; i < array.length; i++) {
      max = Math.max(max, array[i]);
    }
    return max;
  }
  public static float[] concat(float[]... arrays) {
    int length = 0;
    for (float[] array : arrays) {
      length += array.length;
    }
    float[] result = new float[length];
    int pos = 0;
    for (float[] array : arrays) {
      System.arraycopy(array, 0, result, pos, array.length);
      pos += array.length;
    }
    return result;
  }
  public static float[] ensureCapacity(
      float[] array, int minLength, int padding) {
    checkArgument(minLength >= 0, "Invalid minLength: %s", minLength);
    checkArgument(padding >= 0, "Invalid padding: %s", padding);
    return (array.length < minLength)
        ? copyOf(array, minLength + padding)
        : array;
  }
  private static float[] copyOf(float[] original, int length) {
    float[] copy = new float[length];
    System.arraycopy(original, 0, copy, 0, Math.min(original.length, length));
    return copy;
  }
  public static String join(String separator, float... array) {
    checkNotNull(separator);
    if (array.length == 0) {
      return "";
    }
    StringBuilder builder = new StringBuilder(array.length * 12);
    builder.append(array[0]);
    for (int i = 1; i < array.length; i++) {
      builder.append(separator).append(array[i]);
    }
    return builder.toString();
  }
  public static Comparator<float[]> lexicographicalComparator() {
    return LexicographicalComparator.INSTANCE;
  }
  private enum LexicographicalComparator implements Comparator<float[]> {
    INSTANCE;
    public int compare(float[] left, float[] right) {
      int minLength = Math.min(left.length, right.length);
      for (int i = 0; i < minLength; i++) {
        int result = Floats.compare(left[i], right[i]);
        if (result != 0) {
          return result;
        }
      }
      return left.length - right.length;
    }
  }
  public static float[] toArray(Collection<Float> collection) {
    if (collection instanceof FloatArrayAsList) {
      return ((FloatArrayAsList) collection).toFloatArray();
    }
    Object[] boxedArray = collection.toArray();
    int len = boxedArray.length;
    float[] array = new float[len];
    for (int i = 0; i < len; i++) {
      array[i] = (Float) boxedArray[i];
    }
    return array;
  }
  public static List<Float> asList(float... backingArray) {
    if (backingArray.length == 0) {
      return Collections.emptyList();
    }
    return new FloatArrayAsList(backingArray);
  }
  @GwtCompatible
  private static class FloatArrayAsList extends AbstractList<Float>
      implements RandomAccess, Serializable {
    final float[] array;
    final int start;
    final int end;
    FloatArrayAsList(float[] array) {
      this(array, 0, array.length);
    }
    FloatArrayAsList(float[] array, int start, int end) {
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
    @Override public Float get(int index) {
      checkElementIndex(index, size());
      return array[start + index];
    }
    @Override public boolean contains(Object target) {
      return (target instanceof Float)
          && Floats.indexOf(array, (Float) target, start, end) != -1;
    }
    @Override public int indexOf(Object target) {
      if (target instanceof Float) {
        int i = Floats.indexOf(array, (Float) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }
    @Override public int lastIndexOf(Object target) {
      if (target instanceof Float) {
        int i = Floats.lastIndexOf(array, (Float) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }
    @Override public Float set(int index, Float element) {
      checkElementIndex(index, size());
      float oldValue = array[start + index];
      array[start + index] = element;
      return oldValue;
    }
     public List<Float> subList(int fromIndex, int toIndex) {
      int size = size();
      checkPositionIndexes(fromIndex, toIndex, size);
      if (fromIndex == toIndex) {
        return Collections.emptyList();
      }
      return new FloatArrayAsList(array, start + fromIndex, start + toIndex);
    }
    @Override public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof FloatArrayAsList) {
        FloatArrayAsList that = (FloatArrayAsList) object;
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
        result = 31 * result + Floats.hashCode(array[i]);
      }
      return result;
    }
    @Override public String toString() {
      StringBuilder builder = new StringBuilder(size() * 12);
      builder.append('[').append(array[start]);
      for (int i = start + 1; i < end; i++) {
        builder.append(", ").append(array[i]);
      }
      return builder.append(']').toString();
    }
    float[] toFloatArray() {
      int size = size();
      float[] result = new float[size];
      System.arraycopy(array, start, result, 0, size);
      return result;
    }
    private static final long serialVersionUID = 0;
  }
}
