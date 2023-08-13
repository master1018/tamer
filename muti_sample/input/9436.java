public class ObjectMonitor extends VMObject {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    heap = VM.getVM().getObjectHeap();
    Type type  = db.lookupType("ObjectMonitor");
    sun.jvm.hotspot.types.Field f = type.getField("_header");
    headerFieldOffset = f.getOffset();
    f = type.getField("_object");
    objectFieldOffset = f.getOffset();
    f = type.getField("_owner");
    ownerFieldOffset = f.getOffset();
    f = type.getField("FreeNext");
    FreeNextFieldOffset = f.getOffset();
    countField  = type.getCIntegerField("_count");
    waitersField = type.getCIntegerField("_waiters");
    recursionsField = type.getCIntegerField("_recursions");
  }
  public ObjectMonitor(Address addr) {
    super(addr);
  }
  public Mark header() {
    return new Mark(addr.addOffsetTo(headerFieldOffset));
  }
  public boolean isEntered(sun.jvm.hotspot.runtime.Thread current) {
    Address o = owner();
    if (current.threadObjectAddress().equals(o) ||
        current.isLockOwned(o)) {
      return true;
    }
    return false;
  }
  public Address owner() { return addr.getAddressAt(ownerFieldOffset); }
  public long    waiters() { return waitersField.getValue(addr); }
  public Address freeNext() { return addr.getAddressAt(FreeNextFieldOffset); }
  public long count() { return countField.getValue(addr); }
  public long recursions() { return recursionsField.getValue(addr); }
  public OopHandle object() {
    return addr.getOopHandleAt(objectFieldOffset);
  }
  public long contentions() {
      long count = count();
      if (VM.getVM().getOS().equals("win32")) {
          return count > 0? count - 1 : 0;
      } else {
          return count;
      }
  }
  private static ObjectHeap    heap;
  private static long          headerFieldOffset;
  private static long          objectFieldOffset;
  private static long          ownerFieldOffset;
  private static long          FreeNextFieldOffset;
  private static CIntegerField countField;
  private static CIntegerField waitersField;
  private static CIntegerField recursionsField;
}
