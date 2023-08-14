public class RemoteX86ThreadContext extends X86ThreadContext {
  private RemoteDebuggerClient debugger;
  public RemoteX86ThreadContext(RemoteDebuggerClient debugger) {
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
