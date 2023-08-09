final public class LinuxSPARCCFrame extends BasicCFrame {
   public LinuxSPARCCFrame(LinuxDebugger dbg, Address sp, Address pc, int address_size) {
      super(dbg.getCDebugger());
      this.sp = sp;
      this.pc = pc;
      this.dbg = dbg;
      this.address_size=address_size;
      if (address_size==8) SPARC_STACK_BIAS = 0x7ff;
      else SPARC_STACK_BIAS = 0x0;
   }
   public ClosestSymbol closestSymbolToPC() {
      return dbg.lookup(dbg.getAddressValue(pc()));
   }
   public Address pc() {
      return     pc;
   }
   public Address localVariableBase() {
      return sp;
   }
   public CFrame sender() {
      if (sp == null) {
        return null;
      }
      Address nextSP = sp.getAddressAt( SPARCThreadContext.R_SP * address_size + SPARC_STACK_BIAS);
      if (nextSP == null) {
        return null;
      }
      Address nextPC  = sp.getAddressAt(SPARCThreadContext.R_O7 * address_size + SPARC_STACK_BIAS);
      if (nextPC == null) {
        return null;
      }
      return new LinuxSPARCCFrame(dbg, nextSP, nextPC,address_size);
   }
   public static int SPARC_STACK_BIAS;
   private static int address_size;
   private Address pc;
   private Address sp;
   private LinuxDebugger dbg;
}
