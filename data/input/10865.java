final public class LinuxX86CFrame extends BasicCFrame {
   public LinuxX86CFrame(LinuxDebugger dbg, Address ebp, Address pc) {
      super(dbg.getCDebugger());
      this.ebp = ebp;
      this.pc = pc;
      this.dbg = dbg;
   }
   public ClosestSymbol closestSymbolToPC() {
      return dbg.lookup(dbg.getAddressValue(pc()));
   }
   public Address pc() {
      return pc;
   }
   public Address localVariableBase() {
      return ebp;
   }
   public CFrame sender() {
      if (ebp == null) {
        return null;
      }
      Address nextEBP = ebp.getAddressAt( 0 * ADDRESS_SIZE);
      if (nextEBP == null) {
        return null;
      }
      Address nextPC  = ebp.getAddressAt( 1 * ADDRESS_SIZE);
      if (nextPC == null) {
        return null;
      }
      return new LinuxX86CFrame(dbg, nextEBP, nextPC);
   }
   private static final int ADDRESS_SIZE = 4;
   private Address pc;
   private Address ebp;
   private LinuxDebugger dbg;
}
