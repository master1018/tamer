public final class UnsignedBytes {
  private UnsignedBytes() {}
  public static byte checkedCast(long value) {
    checkArgument(value >> 8 == 0, "out of range: %s", value);
    return (byte) value;
  }
  public static byte saturatedCast(long value) {
    if (value > 255) {
      return (byte) 255; 
    }
    if (value < 0) {
      return (byte) 0;
    }
    return (byte) value;
  }
  public static int compare(byte a, byte b) {
    return (a & 0xFF) - (b & 0xFF);
  }
  public static byte min(byte... array) {
    checkArgument(array.length > 0);
    int min = array[0] & 0xFF;
    for (int i = 1; i < array.length; i++) {
      int next = array[i] & 0xFF;
      if (next < min) {
        min = next;
      }
    }
    return (byte) min;
  }
  public static byte max(byte... array) {
    checkArgument(array.length > 0);
    int max = array[0] & 0xFF;
    for (int i = 1; i < array.length; i++) {
      int next = array[i] & 0xFF;
      if (next > max) {
        max = next;
      }
    }
    return (byte) max;
  }
  public static String join(String separator, byte... array) {
    checkNotNull(separator);
    if (array.length == 0) {
      return "";
    }
    StringBuilder builder = new StringBuilder(array.length * 5);
    builder.append(array[0] & 0xFF);
    for (int i = 1; i < array.length; i++) {
      builder.append(separator).append(array[i] & 0xFF);
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
        int result = UnsignedBytes.compare(left[i], right[i]);
        if (result != 0) {
          return result;
        }
      }
      return left.length - right.length;
    }
  }
}
