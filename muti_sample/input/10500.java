public class JNIid extends VMObject {
  private static sun.jvm.hotspot.types.OopField holder;
  private static AddressField next;
  private static CIntegerField offset;
  private static sun.jvm.hotspot.types.OopField resolvedMethod;
  private static sun.jvm.hotspot.types.OopField resolvedReceiver;
  private ObjectHeap heap;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
  }
  public JNIid(Address addr, ObjectHeap heap) {
    super(addr);
    this.heap = heap;
  }
  public JNIid next() {
    Address nextAddr = next.getValue(addr);
    if (nextAddr == null) {
      return null;
    }
    return new JNIid(nextAddr, heap);
  }
  public Klass     holder()           { return (Klass) heap.newOop(holder.getValue(addr)); }
  public int       offset()           { return (int) offset.getValue(addr); }
  public Method    method() {
    return (Method) ((InstanceKlass) holder()).getMethods().getObjAt(offset());
  }
  public Method    resolvedMethod()   { return (Method) heap.newOop(resolvedMethod.getValue(addr)); }
  public Klass     resolvedReceiver() { return (Klass) heap.newOop(resolvedReceiver.getValue(addr)); }
}
