public class X86CFrame extends BasicCFrame {
  private Address ebp;
  private Address pc;
  private static final int ADDRESS_SIZE = 4;
  public X86CFrame(CDebugger dbg, Address ebp, Address pc) {
    super(dbg);
    this.ebp = ebp;
    this.pc  = pc;
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
    return new X86CFrame(dbg(), nextEBP, nextPC);
  }
  public Address pc() {
    return pc;
  }
  public Address localVariableBase() {
    return ebp;
  }
}
