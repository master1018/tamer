public class PCRelativeAddress extends IndirectAddress {
   private final long disp;
   public PCRelativeAddress(long disp) {
       this.disp = disp;
   }
   public String toString() {
      return new Long(disp).toString();
   }
   public long getDisplacement() {
      return disp;
   }
}
