public final class X86IllegalInstruction extends X86Instruction {
   final private String description;
   public X86IllegalInstruction() {
      super("illegal", 1, 0);
      description = "bad opcode";
   }
   public String asString(long currentPc, SymbolFinder symFinder) {
      return description;
   }
   public boolean isIllegal() {
      return true;
   }
}
