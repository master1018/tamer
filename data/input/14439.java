public class HashtableEntry extends BasicHashtableEntry {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("HashtableEntry<intptr_t>");
    literalField   = type.getAddressField("_literal");
  }
  private static AddressField      literalField;
  public Address literalValue() {
    return literalField.getValue(addr);
  }
  public HashtableEntry(Address addr) {
    super(addr);
  }
}
