public class ExceptionBlob extends SingletonBlob {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("ExceptionBlob");
  }
  public ExceptionBlob(Address addr) {
    super(addr);
  }
  public boolean isExceptionStub() {
    return true;
  }
}
