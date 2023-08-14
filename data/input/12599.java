public class DbxSPARCThreadContext extends SPARCThreadContext {
  private DbxDebugger debugger;
  public DbxSPARCThreadContext(DbxDebugger debugger) {
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
