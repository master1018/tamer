public class FreeList extends VMObject {
   static {
      VM.registerVMInitializedObserver(new Observer() {
         public void update(Observable o, Object data) {
            initialize(VM.getVM().getTypeDataBase());
         }
      });
   }
   private static synchronized void initialize(TypeDataBase db) {
      Type type = db.lookupType("FreeList");
      sizeField = type.getCIntegerField("_size");
      countField = type.getCIntegerField("_count");
      headerSize = type.getSize();
   }
   private static CIntegerField sizeField;
   private static CIntegerField countField;
   private static long          headerSize;
   public FreeList(Address address) {
     super(address);
   }
   public long size() {
      return sizeField.getValue(addr);
   }
   public long count() {
      return  countField.getValue(addr);
   }
   public static long sizeOf() {
     return headerSize;
  }
}
