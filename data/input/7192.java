public abstract class CompactibleSpace extends Space {
  private static AddressField compactionTopField;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("CompactibleSpace");
    compactionTopField = type.getAddressField("_compaction_top");
  }
  public CompactibleSpace(Address addr) {
    super(addr);
  }
  public Address compactionTop() {
    return compactionTopField.getValue(addr);
  }
}
