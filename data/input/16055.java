public class X86BranchInstruction extends X86Instruction
                                        implements BranchInstruction {
   final private X86PCRelativeAddress addr;
   public X86BranchInstruction(String name, X86PCRelativeAddress addr, int size, int prefixes) {
      super(name, size, prefixes);
      this.addr = addr;
      if(addr instanceof X86PCRelativeAddress) {
         addr.setInstructionSize(getSize());
      }
   }
   public String asString(long currentPc, SymbolFinder symFinder) {
      StringBuffer buf = new StringBuffer();
      buf.append(getPrefixString());
      buf.append(getName());
      if(addr != null) {
         buf.append(spaces);
         if(addr instanceof X86PCRelativeAddress) {
            long disp = ((X86PCRelativeAddress)addr).getDisplacement();
            long address = disp + currentPc;
            buf.append(symFinder.getSymbolFor(address));
         }
      }
      return buf.toString();
   }
   public Address getBranchDestination() {
      return addr;
   }
   public boolean isBranch() {
      return true;
   }
   public boolean isConditional() {
      return false;
   }
}
