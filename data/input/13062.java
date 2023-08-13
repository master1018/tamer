public class BasicHashtableEntry extends VMObject {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("BasicHashtableEntry");
    hashField      = type.getCIntegerField("_hash");
    nextField      = type.getAddressField("_next");
  }
  private static CIntegerField hashField;
  private static AddressField  nextField;
  public long hash() {
    return hashField.getValue(addr) & 0xFFFFFFFFL;
  }
  private long nextAddressValue() {
    Debugger dbg = VM.getVM().getDebugger();
    Address nextValue = nextField.getValue(addr);
    return (nextValue != null) ? dbg.getAddressValue(nextValue) : 0L;
  }
  public boolean isShared() {
    return (nextAddressValue() & 1L) != 0;
  }
  public BasicHashtableEntry next() {
    Address nextValue = nextField.getValue(addr);
    Address next = (nextValue != null)? nextValue.andWithMask(-2L) : null;
    return (BasicHashtableEntry) VMObjectFactory.newObject(this.getClass(), next);
  }
  public BasicHashtableEntry(Address addr) {
    super(addr);
  }
}
