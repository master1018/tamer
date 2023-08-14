public abstract class MachineDescriptionTwosComplement {
  private static final long[] signedMinValues = {
    Byte.MIN_VALUE,
    Short.MIN_VALUE,
    Integer.MIN_VALUE,
    Long.MIN_VALUE
  };
  private static final long[] signedMaxValues = {
    Byte.MAX_VALUE,
    Short.MAX_VALUE,
    Integer.MAX_VALUE,
    Long.MAX_VALUE
  };
  private static final long[] unsignedMaxValues = {
    255L,
    65535L,
    4294967295L,
    -1L
  };
  public long cIntegerTypeMaxValue(long sizeInBytes, boolean isUnsigned) {
    if (isUnsigned) {
      return tableLookup(sizeInBytes, unsignedMaxValues);
    } else {
      return tableLookup(sizeInBytes, signedMaxValues);
    }
  };
  public long cIntegerTypeMinValue(long sizeInBytes, boolean isUnsigned) {
    if (isUnsigned) {
      return 0;
    }
    return tableLookup(sizeInBytes, signedMinValues);
  }
  public boolean isLP64() {
    return false;
  }
  private long tableLookup(long sizeInBytes, long[] table) {
    switch ((int) sizeInBytes) {
    case 1:
      return table[0];
    case 2:
      return table[1];
    case 4:
      return table[2];
    case 8:
      return table[3];
    default:
      throw new IllegalArgumentException("C integer type of " + sizeInBytes + " not supported");
    }
  }
}
