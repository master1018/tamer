public final class SignedBytes {
  private SignedBytes() {}
  public static byte checkedCast(long value) {
    byte result = (byte) value;
    checkArgument(result == value, "Out of range: %s", value);
    return result;
  }
  public static byte saturatedCast(long value) {
    if (value > Byte.MAX_VALUE) {
      return Byte.MAX_VALUE;
    }
    if (value < Byte.MIN_VALUE) {
      return Byte.MIN_VALUE;
    }
    return (byte) value;
  }
  public static int compare(byte a, byte b) {
    return a - b; 
  }
  public static byte min(byte... array) {
    checkArgument(array.length > 0);
    byte min = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] < min) {
        min = array[i];
      }
    }
    return min;
  }
  public static byte max(byte... array) {
    checkArgument(array.length > 0);
    byte max = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] > max) {
        max = array[i];
      }
    }
    return max;
  }
  public static String join(String separator, byte... array) {
    checkNotNull(separator);
    if (array.length == 0) {
      return "";
    }
    StringBuilder builder = new StringBuilder(array.length * 5);
    builder.append(array[0]);
    for (int i = 1; i < array.length; i++) {
      builder.append(separator).append(array[i]);
    }
    return builder.toString();
  }
  public static Comparator<byte[]> lexicographicalComparator() {
    return LexicographicalComparator.INSTANCE;
  }
  private enum LexicographicalComparator implements Comparator<byte[]> {
    INSTANCE;
    public int compare(byte[] left, byte[] right) {
      int minLength = Math.min(left.length, right.length);
      for (int i = 0; i < minLength; i++) {
        int result = SignedBytes.compare(left[i], right[i]);
        if (result != 0) {
          return result;
        }
      }
      return left.length - right.length;
    }
  }
}
