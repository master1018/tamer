public class CMSPermGen extends PermGen {
   private static AddressField genField;
   static {
      VM.registerVMInitializedObserver(new Observer() {
         public void update(Observable o, Object data) {
            initialize(VM.getVM().getTypeDataBase());
         }
      });
   }
   private static synchronized void initialize(TypeDataBase db) {
      Type type = db.lookupType("CMSPermGen");
      genField = type.getAddressField("_gen");
   }
   public CMSPermGen(Address addr) {
      super(addr);
   }
   public Generation asGen() {
      return GenerationFactory.newObject(genField.getValue(addr));
   }
}
