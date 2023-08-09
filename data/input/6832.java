public class GenerationFactory {
  private static VirtualConstructor ctor;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    ctor = new VirtualConstructor(db);
    ctor.addMapping("CompactingPermGenGen", CompactingPermGenGen.class);
    ctor.addMapping("CMSPermGenGen", CMSPermGenGen.class);
    ctor.addMapping("DefNewGeneration", DefNewGeneration.class);
    ctor.addMapping("ParNewGeneration", ParNewGeneration.class);
    ctor.addMapping("TenuredGeneration", TenuredGeneration.class);
    ctor.addMapping("ConcurrentMarkSweepGeneration", ConcurrentMarkSweepGeneration.class);
  }
  public static Generation newObject(Address addr) {
      try {
          return (Generation) ctor.instantiateWrapperFor(addr);
      } catch (WrongTypeException e) {
          return new Generation(addr) {
                  public String name() {
                      return "unknown generation type";
                  }
                  public void spaceIterate(SpaceClosure blk, boolean usedOnly) {
                  }
                  public void printOn(java.io.PrintStream tty) {
                      tty.println("unknown subtype of Generation @ " + getAddress() + " (" +
                                  virtualSpace().low() + "," + virtualSpace().high() + ")");
                  }
                  public long used() {
                      return 0;
                  }
                  public long free() {
                      return 0;
                  }
                  public long capacity() {
                      return 0;
                  }
                  public long contiguousAvailable() {
                      return 0;
                  }
              };
      }
  }
}
