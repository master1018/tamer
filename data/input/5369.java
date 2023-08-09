public class PlaceholderEntry extends sun.jvm.hotspot.utilities.HashtableEntry {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("PlaceholderEntry");
    loaderField = type.getOopField("_loader");
  }
  private static sun.jvm.hotspot.types.OopField loaderField;
  public Oop loader() {
    return VM.getVM().getObjectHeap().newOop(loaderField.getValue(addr));
  }
  public PlaceholderEntry(Address addr) {
    super(addr);
  }
  public Symbol klass() {
    return Symbol.create(literalValue());
  }
}
