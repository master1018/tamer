public class AMD64Helper implements CPUHelper {
   public Disassembler createDisassembler(long startPc, byte[] code) {
      return null;
   }
   public Register getIntegerRegister(int num) {
      return AMD64Registers.getRegister(num);
   }
   public Register getFloatRegister(int num) {
      return AMD64FloatRegisters.getRegister(num);
   }
   public Register getStackPointer() {
      return AMD64Registers.RSP;
   }
   public Register getFramePointer() {
      return AMD64Registers.RBP;
   }
}
