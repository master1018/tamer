public class UncommonTrapBlob extends SingletonBlob {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("UncommonTrapBlob");
  }
  public UncommonTrapBlob(Address addr) {
    super(addr);
  }
  public boolean isUncommonTrapStub() {
    return true;
  }
}
