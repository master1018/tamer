public class BinaryTreeDictionary extends VMObject {
   static {
      VM.registerVMInitializedObserver(new Observer() {
         public void update(Observable o, Object data) {
            initialize(VM.getVM().getTypeDataBase());
         }
      });
   }
   private static synchronized void initialize(TypeDataBase db) {
      Type type = db.lookupType("BinaryTreeDictionary");
      totalSizeField = type.getCIntegerField("_totalSize");
   }
   private static CIntegerField totalSizeField;
   public long size() {
      return totalSizeField.getValue(addr);
   }
   public BinaryTreeDictionary(Address addr) {
      super(addr);
   }
}
