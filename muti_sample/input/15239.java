class WindbgX86ThreadContext extends X86ThreadContext {
  private WindbgDebugger debugger;
  public WindbgX86ThreadContext(WindbgDebugger debugger) {
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
