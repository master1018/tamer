class WindbgAMD64ThreadContext extends AMD64ThreadContext {
  private WindbgDebugger debugger;
  public WindbgAMD64ThreadContext(WindbgDebugger debugger) {
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
