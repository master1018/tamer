public class RemoteX86Thread extends RemoteThread  {
  public RemoteX86Thread(RemoteDebuggerClient debugger, Address addr) {
     super(debugger, addr);
  }
  public RemoteX86Thread(RemoteDebuggerClient debugger, long id) {
     super(debugger, id);
  }
  public ThreadContext getContext() throws IllegalThreadStateException {
    RemoteX86ThreadContext context = new RemoteX86ThreadContext(debugger);
    long[] regs = (addr != null)? debugger.getThreadIntegerRegisterSet(addr) :
                                  debugger.getThreadIntegerRegisterSet(id);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(regs.length == X86ThreadContext.NPRGREG, "size of register set must match");
    }
    for (int i = 0; i < regs.length; i++) {
      context.setRegister(i, regs[i]);
    }
    return context;
  }
}
