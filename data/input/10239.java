public class SingletonBlob extends CodeBlob {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("SingletonBlob");
  }
  public SingletonBlob(Address addr) {
    super(addr);
  }
  public boolean isSingletonBlob()      { return true; }
}
