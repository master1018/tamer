public abstract class Operand {
   public boolean isAddress() {
      return false;
   }
   public boolean isImmediate() {
      return false;
   }
   public boolean isRegister() {
      return false;
   }
}
