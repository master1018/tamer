public class CMSCollector extends VMObject {
  private static long markBitMapFieldOffset;
  public CMSCollector(Address addr) {
    super(addr);
  }
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("CMSCollector");
    markBitMapFieldOffset = type.getField("_markBitMap").getOffset();
  }
  public CMSBitMap markBitMap() {
   return (CMSBitMap) VMObjectFactory.newObject(
                                CMSBitMap.class,
                                addr.addOffsetTo(markBitMapFieldOffset));
  }
  public long blockSizeUsingPrintezisBits(Address addr) {
    CMSBitMap markBitMap = markBitMap();
    long addressSize = VM.getVM().getAddressSize();
    if ( markBitMap.isMarked(addr) &&  markBitMap.isMarked(addr.addOffsetTo(1*addressSize)) ) {
       System.err.println("Printezis bits are set...");
      Address nextOneAddr = markBitMap.getNextMarkedWordAddress(addr.addOffsetTo(2*addressSize));
      long size =  (nextOneAddr.addOffsetTo(1*addressSize)).minus(addr);
      return size;
    } else {
     System.err.println("Missing Printszis marks...");
     return -1;
    }
  }
}
