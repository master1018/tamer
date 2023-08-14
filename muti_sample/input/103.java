final class ProcCFrame extends BasicCFrame {
   public Address pc() {
      return pc;
   }
   public Address localVariableBase() {
      return fp;
   }
   public CFrame sender() {
      return sender;
   }
   public ClosestSymbol closestSymbolToPC() {
      return procDbg.lookup(procDbg.getAddressValue(pc));
   }
   ProcCFrame(ProcDebugger dbg, Address pc, Address fp) {
      super(dbg.getCDebugger());
      this.pc = pc;
      this.fp = fp;
      this.procDbg = dbg;
   }
   void setSender(ProcCFrame sender) {
      this.sender = sender;
   }
   private Address    pc;
   private Address    fp;
   private ProcCFrame sender;
   private ProcDebugger procDbg;
}
