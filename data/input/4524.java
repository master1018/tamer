public abstract class Disassembler {
   protected long   startPc;
   protected byte[] code;
   public Disassembler(long startPc, byte[] code) {
      this.startPc = startPc;
      this.code = code;
   }
   public long getStartPC() {
      return startPc;
   }
   public byte[] getCode() {
      return code;
   }
   public abstract void decode(InstructionVisitor visitor);
}
