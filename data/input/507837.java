final class RegularImmutableSet<E> extends ArrayImmutableSet<E> {
  @VisibleForTesting final transient Object[] table;
  private final transient int mask;
  private final transient int hashCode;
  RegularImmutableSet(
      Object[] elements, int hashCode, Object[] table, int mask) {
    super(elements);
    this.table = table;
    this.mask = mask;
    this.hashCode = hashCode;
  }
  @Override public boolean contains(Object target) {
    if (target == null) {
      return false;
    }
    for (int i = Hashing.smear(target.hashCode()); true; i++) {
      Object candidate = table[i & mask];
      if (candidate == null) {
        return false;
      }
      if (candidate.equals(target)) {
        return true;
      }
    }
  }
  @Override public int hashCode() {
    return hashCode;
  }
  @Override boolean isHashCodeFast() {
    return true;
  }
}
