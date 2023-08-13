public class CompactingPermGen extends PermGen {
  private static AddressField genField;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("CompactingPermGen");
    genField = type.getAddressField("_gen");
  }
  public CompactingPermGen(Address addr) {
    super(addr);
  }
  public Generation asGen() {
    return GenerationFactory.newObject(genField.getValue(addr));
  }
}
