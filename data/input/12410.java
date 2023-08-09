public class ConstantPoolCache extends Oop {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type      = db.lookupType("constantPoolCacheOopDesc");
    constants      = new OopField(type.getOopField("_constant_pool"), 0);
    baseOffset     = type.getSize();
    Type elType    = db.lookupType("ConstantPoolCacheEntry");
    elementSize    = elType.getSize();
    length         = new CIntField(type.getCIntegerField("_length"), 0);
  }
  ConstantPoolCache(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }
  public boolean isConstantPoolCache() { return true; }
  private static OopField constants;
  private static long baseOffset;
  private static long elementSize;
  private static CIntField length;
  public ConstantPool getConstants() { return (ConstantPool) constants.getValue(this); }
  public long getObjectSize() {
    return alignObjectSize(baseOffset + getLength() * elementSize);
  }
  public ConstantPoolCacheEntry getEntryAt(int i) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(0 <= i && i < getLength(), "index out of bounds");
    }
    return new ConstantPoolCacheEntry(this, i);
  }
  public static boolean isSecondaryIndex(int i)     { return (i < 0); }
  public static int     decodeSecondaryIndex(int i) { return  isSecondaryIndex(i) ? ~i : i; }
  public static int     encodeSecondaryIndex(int i) { return !isSecondaryIndex(i) ? ~i : i; }
  public ConstantPoolCacheEntry getSecondaryEntryAt(int i) {
    ConstantPoolCacheEntry e = new ConstantPoolCacheEntry(this, decodeSecondaryIndex(i));
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(e.isSecondaryEntry(), "must be a secondary entry");
    }
    return e;
  }
  public ConstantPoolCacheEntry getMainEntryAt(int i) {
    if (isSecondaryIndex(i)) {
      i = getSecondaryEntryAt(i).getMainEntryIndex();
    }
    ConstantPoolCacheEntry e = new ConstantPoolCacheEntry(this, i);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(!e.isSecondaryEntry(), "must not be a secondary entry");
    }
    return e;
  }
  public int getIntAt(int entry, int fld) {
    long offset = baseOffset + entry * elementSize + fld* getHeap().getIntSize();
    return (int) getHandle().getCIntegerAt(offset, getHeap().getIntSize(), true );
  }
  public void printValueOn(PrintStream tty) {
    tty.print("ConstantPoolCache for " + getConstants().getPoolHolder().getName().asString());
  }
  public int getLength() {
    return (int) length.getValue(this);
  }
  public void iterateFields(OopVisitor visitor, boolean doVMFields) {
    super.iterateFields(visitor, doVMFields);
    if (doVMFields) {
      visitor.doOop(constants, true);
      for (int i = 0; i < getLength(); i++) {
        ConstantPoolCacheEntry entry = getEntryAt(i);
        entry.iterateFields(visitor);
      }
    }
  }
};
