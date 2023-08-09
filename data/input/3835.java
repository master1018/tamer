public class java_lang_Class {
  static OopField klassField;
  static IntField oopSizeField;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type jlc = db.lookupType("java_lang_Class");
    int klassOffset = (int) jlc.getCIntegerField("klass_offset").getValue();
    if (VM.getVM().isCompressedOopsEnabled()) {
      klassField = new NarrowOopField(new NamedFieldIdentifier("klass"), klassOffset, true);
    } else {
      klassField = new OopField(new NamedFieldIdentifier("klass"), klassOffset, true);
    }
    int oopSizeOffset = (int) jlc.getCIntegerField("oop_size_offset").getValue();
    oopSizeField = new IntField(new NamedFieldIdentifier("oop_size"), oopSizeOffset, true);
  }
  public static Klass asKlass(Oop aClass) {
    return (Klass) java_lang_Class.klassField.getValue(aClass);
  }
  public static long getOopSize(Oop aClass) {
    return java_lang_Class.oopSizeField.getValue(aClass);
  }
}
