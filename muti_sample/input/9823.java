public class ProcSPARCThread implements ThreadProxy {
  private ProcDebugger debugger;
  private int         id;
  public ProcSPARCThread(ProcDebugger debugger, Address addr) {
    this.debugger = debugger;
    this.id       = (int) addr.getCIntegerAt(0, 4, true);
  }
  public ProcSPARCThread(ProcDebugger debugger, long id) {
    this.debugger = debugger;
    this.id  = (int) id;
  }
  public ThreadContext getContext() throws IllegalThreadStateException {
    ProcSPARCThreadContext context = new ProcSPARCThreadContext(debugger);
    long[] regs = debugger.getThreadIntegerRegisterSet(id);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(regs.length == SPARCThreadContext.NPRGREG, "size of register set must match");
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
    if ((obj == null) || !(obj instanceof ProcSPARCThread)) {
      return false;
    }
    return (((ProcSPARCThread) obj).id == id);
  }
  public int hashCode() {
    return id;
  }
}
