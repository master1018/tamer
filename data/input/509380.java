final class Hashing {
  private Hashing() {}
  static int smear(int hashCode) {
    hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
    return hashCode ^ (hashCode >>> 7) ^ (hashCode >>> 4);
  }
  private static final int MAX_TABLE_SIZE = 1 << 30;
  private static final int CUTOFF = 1 << 29;
  static int chooseTableSize(int setSize) {
    if (setSize < CUTOFF) {
      return Integer.highestOneBit(setSize) << 2;
    }
    checkArgument(setSize < MAX_TABLE_SIZE, "collection too large");
    return MAX_TABLE_SIZE;
  }
}
