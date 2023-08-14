public class Universe {
  private static AddressField collectedHeapField;
  private static VirtualConstructor heapConstructor;
  private static sun.jvm.hotspot.types.OopField mainThreadGroupField;
  private static sun.jvm.hotspot.types.OopField systemThreadGroupField;
  private static sun.jvm.hotspot.types.OopField boolArrayKlassObjField;
  private static sun.jvm.hotspot.types.OopField byteArrayKlassObjField;
  private static sun.jvm.hotspot.types.OopField charArrayKlassObjField;
  private static sun.jvm.hotspot.types.OopField intArrayKlassObjField;
  private static sun.jvm.hotspot.types.OopField shortArrayKlassObjField;
  private static sun.jvm.hotspot.types.OopField longArrayKlassObjField;
  private static sun.jvm.hotspot.types.OopField singleArrayKlassObjField;
  private static sun.jvm.hotspot.types.OopField doubleArrayKlassObjField;
  private static sun.jvm.hotspot.types.OopField systemObjArrayKlassObjField;
  private static AddressField narrowOopBaseField;
  private static CIntegerField narrowOopShiftField;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("Universe");
    collectedHeapField = type.getAddressField("_collectedHeap");
    heapConstructor = new VirtualConstructor(db);
    heapConstructor.addMapping("GenCollectedHeap", GenCollectedHeap.class);
    heapConstructor.addMapping("ParallelScavengeHeap", ParallelScavengeHeap.class);
    mainThreadGroupField   = type.getOopField("_main_thread_group");
    systemThreadGroupField = type.getOopField("_system_thread_group");
    boolArrayKlassObjField = type.getOopField("_boolArrayKlassObj");
    byteArrayKlassObjField = type.getOopField("_byteArrayKlassObj");
    charArrayKlassObjField = type.getOopField("_charArrayKlassObj");
    intArrayKlassObjField = type.getOopField("_intArrayKlassObj");
    shortArrayKlassObjField = type.getOopField("_shortArrayKlassObj");
    longArrayKlassObjField = type.getOopField("_longArrayKlassObj");
    singleArrayKlassObjField = type.getOopField("_singleArrayKlassObj");
    doubleArrayKlassObjField = type.getOopField("_doubleArrayKlassObj");
    systemObjArrayKlassObjField = type.getOopField("_systemObjArrayKlassObj");
    narrowOopBaseField = type.getAddressField("_narrow_oop._base");
    narrowOopShiftField = type.getCIntegerField("_narrow_oop._shift");
  }
  public Universe() {
  }
  public CollectedHeap heap() {
    try {
      return (CollectedHeap) heapConstructor.instantiateWrapperFor(collectedHeapField.getValue());
    } catch (WrongTypeException e) {
      return new CollectedHeap(collectedHeapField.getValue());
    }
  }
  public static long getNarrowOopBase() {
    if (narrowOopBaseField.getValue() == null) {
      return 0;
    } else {
      return narrowOopBaseField.getValue().minus(null);
    }
  }
  public static int getNarrowOopShift() {
    return (int)narrowOopShiftField.getValue();
  }
  public boolean isIn(Address p) {
    return heap().isIn(p);
  }
  public boolean isInReserved(Address p) {
    return heap().isInReserved(p);
  }
  private Oop newOop(OopHandle handle) {
    return VM.getVM().getObjectHeap().newOop(handle);
  }
  public Oop mainThreadGroup() {
    return newOop(mainThreadGroupField.getValue());
  }
  public Oop systemThreadGroup() {
    return newOop(systemThreadGroupField.getValue());
  }
  public Oop systemObjArrayKlassObj() {
    return newOop(systemObjArrayKlassObjField.getValue());
  }
  public void basicTypeClassesDo(SystemDictionary.ClassVisitor visitor) {
    visitor.visit((Klass)newOop(boolArrayKlassObjField.getValue()));
    visitor.visit((Klass)newOop(byteArrayKlassObjField.getValue()));
    visitor.visit((Klass)newOop(charArrayKlassObjField.getValue()));
    visitor.visit((Klass)newOop(intArrayKlassObjField.getValue()));
    visitor.visit((Klass)newOop(shortArrayKlassObjField.getValue()));
    visitor.visit((Klass)newOop(longArrayKlassObjField.getValue()));
    visitor.visit((Klass)newOop(singleArrayKlassObjField.getValue()));
    visitor.visit((Klass)newOop(doubleArrayKlassObjField.getValue()));
  }
  public void print() { printOn(System.out); }
  public void printOn(PrintStream tty) {
    heap().printOn(tty);
  }
  public static boolean elementTypeShouldBeAligned(BasicType type) {
    return type == BasicType.T_DOUBLE || type == BasicType.T_LONG;
  }
  public static boolean fieldTypeShouldBeAligned(BasicType type) {
    return type == BasicType.T_DOUBLE || type == BasicType.T_LONG;
  }
}
