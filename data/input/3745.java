public class ProcX86Thread implements ThreadProxy {
  private ProcDebugger debugger;
  private int         id;
  public ProcX86Thread(ProcDebugger debugger, Address addr) {
    this.debugger = debugger;
    this.id       = (int) addr.getCIntegerAt(0, 4, true);
  }
  public ProcX86Thread(ProcDebugger debugger, long id) {
    this.debugger = debugger;
    this.id = (int) id;
  }
  public ThreadContext getContext() throws IllegalThreadStateException {
    ProcX86ThreadContext context = new ProcX86ThreadContext(debugger);
    long[] regs = debugger.getThreadIntegerRegisterSet(id);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(regs.length <= X86ThreadContext.NPRGREG, "size of register set is greater than " + X86ThreadContext.NPRGREG);
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
  public boolean equals(Object obj) {
    if ((obj == null) || !(obj instanceof ProcX86Thread)) {
      return false;
    }
    return (((ProcX86Thread) obj).id == id);
  }
  public int hashCode() {
    return id;
  }
}
