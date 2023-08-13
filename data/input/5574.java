class SharedObject extends DSO {
  SharedObject(LinuxDebugger dbg, String filename, long size, Address relocation) {
    super(filename, size, relocation);
    this.dbg     = dbg;
  }
  protected Address newAddress(long address) {
    return dbg.newAddress(address);
  }
  protected long getAddressValue(Address addr) {
    return dbg.getAddressValue(addr);
  }
  private LinuxDebugger   dbg;
}
