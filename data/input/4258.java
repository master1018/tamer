public class RemoteSPARCThreadContext extends SPARCThreadContext {
  private RemoteDebuggerClient debugger;
  public RemoteSPARCThreadContext(RemoteDebuggerClient debugger) {
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
