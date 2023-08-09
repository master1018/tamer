public class RicochetBlob extends SingletonBlob {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
  }
  public RicochetBlob(Address addr) {
    super(addr);
  }
  public boolean isRicochetBlob() {
    return true;
  }
}
