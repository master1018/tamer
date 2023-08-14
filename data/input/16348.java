public class ConstantPoolCacheKlass extends Klass {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type  = db.lookupType("constantPoolCacheKlass");
    headerSize = type.getSize() + Oop.getHeaderSize();
  }
  ConstantPoolCacheKlass(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }
  public long getObjectSize() { return alignObjectSize(headerSize); }
  public void printValueOn(PrintStream tty) {
    tty.print("ConstantPoolCacheKlass");
  }
  private static long headerSize;
}
