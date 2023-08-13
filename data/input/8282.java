public class LinuxSPARCThreadContext extends SPARCThreadContext {
  private LinuxDebugger debugger;
  public LinuxSPARCThreadContext(LinuxDebugger debugger) {
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
