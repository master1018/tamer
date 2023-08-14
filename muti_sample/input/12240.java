class FPStoreDecoder extends FPInstructionDecoder {
   FPStoreDecoder(String name, int addrMode1, int operandType1) {
      super(name, addrMode1, operandType1);
   }
   protected Instruction decodeInstruction(byte[] bytesArray, boolean operandSize, boolean addrSize, X86InstructionFactory factory) {
      Operand op = getOperand1(bytesArray, operandSize, addrSize);
      int size = byteIndex - instrStartIndex;
      return factory.newFPStoreInstruction(name, op, size, prefixes);
   }
}
