public class X86MemoryIndirectAddress extends IndirectAddress {
   private long value;
   public X86MemoryIndirectAddress(long value) {
      this.value = value;
   }
   public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append("*");
      buf.append("[");
      buf.append(Long.toHexString(value));
      buf.append(']');
      return buf.toString();
   }
}
