public class X86DirectAddress extends DirectAddress {
   private long segment;
   public X86DirectAddress(long segment, long disp) {
      super(disp);
      this.segment = segment;
   }
   public X86DirectAddress(long disp) {
      super(disp);
      this.segment = 0;
   }
   public String toString() {
      StringBuffer buf = new StringBuffer();
      if (getSegment() != 0) {
         buf.append("0x");
         buf.append(Long.toHexString(getSegment()));
         buf.append(":");
      }
      buf.append("[");
      buf.append("0x");
      buf.append(Long.toHexString(getValue()));
      buf.append("]");
      return buf.toString();
   }
   long getSegment() {
      return segment;
   }
}
