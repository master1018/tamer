public class RemoteAMD64ThreadContext extends AMD64ThreadContext {
  private RemoteDebuggerClient debugger;
  public RemoteAMD64ThreadContext(RemoteDebuggerClient debugger) {
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
