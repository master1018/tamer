public final class Doubles {
  private Doubles() {}
  public static int hashCode(double value) {
    return ((Double) value).hashCode();
  }
  public static int compare(double a, double b) {
    return Double.compare(a, b);
  }
  public static boolean contains(double[] array, double target) {
    for (double value : array) {
      if (value == target) {
        return true;
      }
    }
    return false;
  }
  public static int indexOf(double[] array, double target) {
    return indexOf(array, target, 0, array.length);
  }
  private static int indexOf(
      double[] array, double target, int start, int end) {
    for (int i = start; i < end; i++) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }
  public static int indexOf(double[] array, double[] target) {
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
  public static int lastIndexOf(double[] array, double target) {
    return lastIndexOf(array, target, 0, array.length);
  }
  private static int lastIndexOf(
      double[] array, double target, int start, int end) {
    for (int i = end - 1; i >= start; i--) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }
  public static double min(double... array) {
    checkArgument(array.length > 0);
    double min = array[0];
    for (int i = 1; i < array.length; i++) {
      min = Math.min(min, array[i]);
    }
    return min;
  }
  public static double max(double... array) {
    checkArgument(array.length > 0);
    double max = array[0];
    for (int i = 1; i < array.length; i++) {
      max = Math.max(max, array[i]);
    }
    return max;
  }
  public static double[] concat(double[]... arrays) {
    int length = 0;
    for (double[] array : arrays) {
      length += array.length;
    }
    double[] result = new double[length];
    int pos = 0;
    for (double[] array : arrays) {
      System.arraycopy(array, 0, result, pos, array.length);
      pos += array.length;
    }
    return result;
  }
  public static double[] ensureCapacity(
      double[] array, int minLength, int padding) {
    checkArgument(minLength >= 0, "Invalid minLength: %s", minLength);
    checkArgument(padding >= 0, "Invalid padding: %s", padding);
    return (array.length < minLength)
        ? copyOf(array, minLength + padding)
        : array;
  }
  private static double[] copyOf(double[] original, int length) {
    double[] copy = new double[length];
    System.arraycopy(original, 0, copy, 0, Math.min(original.length, length));
    return copy;
  }
  public static String join(String separator, double... array) {
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
  public static Comparator<double[]> lexicographicalComparator() {
    return LexicographicalComparator.INSTANCE;
  }
  private enum LexicographicalComparator implements Comparator<double[]> {
    INSTANCE;
    public int compare(double[] left, double[] right) {
      int minLength = Math.min(left.length, right.length);
      for (int i = 0; i < minLength; i++) {
        int result = Doubles.compare(left[i], right[i]);
        if (result != 0) {
          return result;
        }
      }
      return left.length - right.length;
    }
  }
  public static double[] toArray(Collection<Double> collection) {
    if (collection instanceof DoubleArrayAsList) {
      return ((DoubleArrayAsList) collection).toDoubleArray();
    }
    Object[] boxedArray = collection.toArray();
    int len = boxedArray.length;
    double[] array = new double[len];
    for (int i = 0; i < len; i++) {
      array[i] = (Double) boxedArray[i];
    }
    return array;
  }
  public static List<Double> asList(double... backingArray) {
    if (backingArray.length == 0) {
      return Collections.emptyList();
    }
    return new DoubleArrayAsList(backingArray);
  }
  @GwtCompatible
  private static class DoubleArrayAsList extends AbstractList<Double>
      implements RandomAccess, Serializable {
    final double[] array;
    final int start;
    final int end;
    DoubleArrayAsList(double[] array) {
      this(array, 0, array.length);
    }
    DoubleArrayAsList(double[] array, int start, int end) {
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
    @Override public Double get(int index) {
      checkElementIndex(index, size());
      return array[start + index];
    }
    @Override public boolean contains(Object target) {
      return (target instanceof Double)
          && Doubles.indexOf(array, (Double) target, start, end) != -1;
    }
    @Override public int indexOf(Object target) {
      if (target instanceof Double) {
        int i = Doubles.indexOf(array, (Double) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }
    @Override public int lastIndexOf(Object target) {
      if (target instanceof Double) {
        int i = Doubles.lastIndexOf(array, (Double) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }
    @Override public Double set(int index, Double element) {
      checkElementIndex(index, size());
      double oldValue = array[start + index];
      array[start + index] = element;
      return oldValue;
    }
     public List<Double> subList(int fromIndex, int toIndex) {
      int size = size();
      checkPositionIndexes(fromIndex, toIndex, size);
      if (fromIndex == toIndex) {
        return Collections.emptyList();
      }
      return new DoubleArrayAsList(array, start + fromIndex, start + toIndex);
    }
    @Override public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof DoubleArrayAsList) {
        DoubleArrayAsList that = (DoubleArrayAsList) object;
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
        result = 31 * result + Doubles.hashCode(array[i]);
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
    double[] toDoubleArray() {
      int size = size();
      double[] result = new double[size];
      System.arraycopy(array, start, result, 0, size);
      return result;
    }
    private static final long serialVersionUID = 0;
  }
}
