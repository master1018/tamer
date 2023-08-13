public class PSPermGen extends PSOldGen {
   static {
      VM.registerVMInitializedObserver(new Observer() {
         public void update(Observable o, Object data) {
            initialize(VM.getVM().getTypeDataBase());
         }
      });
   }
   private static synchronized void initialize(TypeDataBase db) {
      Type type = db.lookupType("PSPermGen");
   }
   public PSPermGen(Address addr) {
      super(addr);
   }
   public void printOn(PrintStream tty) {
      tty.print("PSPermGen [ ");
      objectSpace().printOn(tty);
      tty.print(" ] ");
   }
}
