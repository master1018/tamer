public class ConstantPoolKlass extends Klass {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type  = db.lookupType("constantPoolKlass");
    headerSize = type.getSize() + Oop.getHeaderSize();
  }
  ConstantPoolKlass(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }
  public long getObjectSize() { return alignObjectSize(headerSize); }
  public void printValueOn(PrintStream tty) {
    tty.print("ConstantPoolKlass");
  }
  private static long headerSize;
}
