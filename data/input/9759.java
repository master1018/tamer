public class MarkBits {
  public MarkBits(CollectedHeap heap) {
    MemRegion reserved = heap.reservedRegion();
    start = reserved.start();
    end   = reserved.end();
    long numOopHandles = end.minus(start) / VM.getVM().getOopSize();
    bits = new BitMap((int) numOopHandles);
  }
  public void clear() {
    bits.clear();
  }
  public boolean mark(Oop obj) {
    if (obj == null) {
      System.err.println("MarkBits: WARNING: null object, ignoring");
      return false;
    }
    OopHandle handle = obj.getHandle();
    long idx = handle.minus(start) / VM.getVM().getOopSize();
    if ((idx < 0) || (idx >= bits.size())) {
      System.err.println("MarkBits: WARNING: object " + handle + " outside of heap, ignoring");
      return false;
    }
    int intIdx = (int) idx;
    if (bits.at(intIdx)) {
      return false; 
    }
    bits.atPut(intIdx, true);
    return true;
  }
  public void clear(Oop obj) {
    OopHandle handle = obj.getHandle();
    long idx = handle.minus(start) / VM.getVM().getOopSize();
    if ((idx < 0) || (idx >= bits.size())) {
      System.err.println("MarkBits: WARNING: object " + handle + " outside of heap, ignoring");
      return;
    }
    int intIdx = (int) idx;
    bits.atPut(intIdx, false);
  }
  private BitMap  bits;
  private Address start;
  private Address end;
}
