public class BufferBlob extends CodeBlob {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("BufferBlob");
  }
  public BufferBlob(Address addr) {
    super(addr);
  }
  public boolean isBufferBlob() {
    return true;
  }
}
