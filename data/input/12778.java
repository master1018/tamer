public class X86Helper implements CPUHelper {
   public Disassembler createDisassembler(long startPc, byte[] code) {
       return new X86Disassembler(startPc, code);
   }
   public Register getIntegerRegister(int num) {
      return X86Registers.getRegister32(num);
   }
   public Register getFloatRegister(int num) {
      return X86FloatRegisters.getRegister(num);
   }
   public Register getStackPointer() {
      return X86Registers.ESP;
   }
   public Register getFramePointer() {
      return X86Registers.EBP;
   }
}
