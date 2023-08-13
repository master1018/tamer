public class X86PCRelativeAddress extends PCRelativeAddress {
   private int instrSize;
   public X86PCRelativeAddress(long disp) {
       super(disp);
   }
   public void setInstructionSize(int size) {
      instrSize = size;
   }
   public String toString() {
      long displacement = this.getDisplacement();
      return new Long(displacement).toString();
   }
   public long getDisplacement() {
      long displacement = super.getDisplacement() + (long)instrSize;
      return displacement;
   }
}
