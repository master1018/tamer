public class Runtime1 {
  private static Field         blobsField;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("Runtime1");
    blobsField = type.getField("_blobs");
  }
  public Runtime1() {
  }
  public Address entryFor(int id) {
    return blobFor(id).codeBegin();
  }
  public CodeBlob blobFor(int id) {
    Address blobAddr = blobsField.getStaticFieldAddress().getAddressAt(id * VM.getVM().getAddressSize());
    return VM.getVM().getCodeCache().createCodeBlobWrapper(blobAddr);
  }
}
