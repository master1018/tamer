public class HashtableBucket extends VMObject {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("HashtableBucket");
    entryField = type.getAddressField("_entry");
  }
  private static AddressField entryField;
  public BasicHashtableEntry getEntry(Class clazz) {
    Address tmp = entryField.getValue(addr);
    return (BasicHashtableEntry) VMObjectFactory.newObject(clazz, tmp);
  }
  public BasicHashtableEntry entry() {
    return getEntry(HashtableEntry.class);
  }
  public HashtableBucket(Address addr) {
    super(addr);
  }
}
