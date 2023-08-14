public class DeoptimizationBlob extends SingletonBlob {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("DeoptimizationBlob");
  }
  public DeoptimizationBlob(Address addr) {
    super(addr);
  }
  public boolean isDeoptimizationStub() {
    return true;
  }
}
