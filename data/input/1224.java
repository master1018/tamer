final public class LinuxAMD64CFrame extends BasicCFrame {
   public LinuxAMD64CFrame(LinuxDebugger dbg, Address rbp, Address rip) {
      super(dbg.getCDebugger());
      this.rbp = rbp;
      this.rip = rip;
      this.dbg = dbg;
   }
   public ClosestSymbol closestSymbolToPC() {
      return dbg.lookup(dbg.getAddressValue(pc()));
   }
   public Address pc() {
      return rip;
   }
   public Address localVariableBase() {
      return rbp;
   }
   public CFrame sender() {
      if (rbp == null) {
        return null;
      }
      Address nextRBP = rbp.getAddressAt( 0 * ADDRESS_SIZE);
      if (nextRBP == null) {
        return null;
      }
      Address nextPC  = rbp.getAddressAt( 1 * ADDRESS_SIZE);
      if (nextPC == null) {
        return null;
      }
      return new LinuxAMD64CFrame(dbg, nextRBP, nextPC);
   }
   private static final int ADDRESS_SIZE = 8;
   private Address rip;
   private Address rbp;
   private LinuxDebugger dbg;
}
