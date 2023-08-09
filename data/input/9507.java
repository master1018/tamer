public class AMD64FloatRegister extends Register {
   public AMD64FloatRegister(int number) {
      super(number);
   }
   public int getNumber() {
      return number;
   }
   public int getNumberOfRegisters() {
      return AMD64FloatRegisters.getNumRegisters();
   }
   public boolean isFloat() {
      return true;
   }
   public boolean isFramePointer() {
      return false;
   }
   public boolean isStackPointer() {
      return false;
   }
   public boolean isValid() {
      return number >= 0 && number < AMD64FloatRegisters.getNumRegisters();
   }
   public String toString() {
      return AMD64FloatRegisters.getRegisterName(number);
   }
}
