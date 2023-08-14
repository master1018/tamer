public class TwoOopHashtable extends Hashtable {
  public TwoOopHashtable(Address addr) {
    super(addr);
  }
  public long computeHash(Symbol name, Oop loader) {
    return ((int) name.identityHash()
        ^  (int) (loader == null ? 0 : loader.identityHash())) & 0xFFFFFFFFL;
  }
  public int indexFor(Symbol name, Oop loader) {
    return hashToIndex(computeHash(name, loader));
  }
}
