public class DirectAddress extends Address {
   private long value;
   public DirectAddress(long value) {
      this.value = value;
   }
   public long getValue() {
      return value;
   }
   public String toString() {
      return Long.toHexString(value);
   }
}
