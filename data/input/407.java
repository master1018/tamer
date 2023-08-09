public class LinuxAMD64ThreadContext extends AMD64ThreadContext {
  private LinuxDebugger debugger;
  public LinuxAMD64ThreadContext(LinuxDebugger debugger) {
    super();
    this.debugger = debugger;
  }
  public void setRegisterAsAddress(int index, Address value) {
    setRegister(index, debugger.getAddressValue(value));
  }
  public Address getRegisterAsAddress(int index) {
    return debugger.newAddress(getRegister(index));
  }
}
