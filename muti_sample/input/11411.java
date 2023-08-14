public class HeapPrinter implements HeapVisitor {
  public HeapPrinter(PrintStream tty) {
    oopPrinter = new OopPrinter(tty);
  }
  private OopPrinter oopPrinter;
  public void prologue(long size) {}
  public boolean doObj(Oop obj) {
          obj.iterate(oopPrinter, true);
          return false;
  }
  public void epilogue() {}
}
