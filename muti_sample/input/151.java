public class DbxX86Thread implements ThreadProxy {
  private DbxDebugger debugger;
  private int         id;
  public DbxX86Thread(DbxDebugger debugger, Address addr) {
    this.debugger = debugger;
    this.id       = (int) addr.getCIntegerAt(0, 4, true);
  }
  public DbxX86Thread(DbxDebugger debugger, long id) {
    this.debugger = debugger;
    this.id  = (int) id;
  }
  public boolean equals(Object obj) {
    if ((obj == null) || !(obj instanceof DbxX86Thread)) {
      return false;
    }
    return (((DbxX86Thread) obj).id == id);
  }
  public int hashCode() {
    return id;
  }
  public ThreadContext getContext() throws IllegalThreadStateException {
    DbxX86ThreadContext context = new DbxX86ThreadContext(debugger);
    long[] regs = debugger.getThreadIntegerRegisterSet(id);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(regs.length == 19, "unknown size of register set -- adjust this code");
    }
    for (int i = 0; i < regs.length; i++) {
      context.setRegister(i, regs[i]);
    }
    return context;
  }
  public boolean canSetContext() throws DebuggerException {
    return false;
  }
  public void setContext(ThreadContext context)
    throws IllegalThreadStateException, DebuggerException {
    throw new DebuggerException("Unimplemented");
  }
  public String toString() {
    return "t@" + id;
  }
}
