public class DefaultHeapVisitor implements HeapVisitor {
  public void prologue(long usedSize) {}
  public boolean doObj(Oop obj)          {return false;}
  public void epilogue()              {}
}
