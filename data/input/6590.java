public class CMSPermGenGen extends ConcurrentMarkSweepGeneration {
   public CMSPermGenGen(Address addr) {
      super(addr);
   }
   public String name() {
      return "concurrent-mark-sweep perm gen";
   }
}
