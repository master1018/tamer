class Win32Thread implements ThreadProxy {
  private Win32Debugger debugger;
  private int           handle;
  private boolean       mustDuplicate;
  private boolean       gotID;
  private int           id;
  Win32Thread(Win32Debugger debugger, Address addr) {
    this.debugger = debugger;
    this.handle   = (int) addr.getCIntegerAt(0, 4, true);
    mustDuplicate = true;
    gotID = false;
  }
  Win32Thread(Win32Debugger debugger, long handle) {
    this.debugger = debugger;
    this.handle   = (int) handle;
    mustDuplicate = false;
    gotID         = false;
  }
  public ThreadContext getContext() throws IllegalThreadStateException {
    if (!debugger.isSuspended()) {
      throw new IllegalThreadStateException("Target process must be suspended");
    }
    long[] data = debugger.getThreadIntegerRegisterSet(handle, mustDuplicate);
    Win32ThreadContext context = new Win32ThreadContext(debugger);
    for (int i = 0; i < data.length; i++) {
      context.setRegister(i, data[i]);
    }
    return context;
  }
  public boolean canSetContext() throws DebuggerException {
    return true;
  }
  public void setContext(ThreadContext thrCtx)
    throws IllegalThreadStateException, DebuggerException {
    if (!debugger.isSuspended()) {
      throw new IllegalThreadStateException("Target process must be suspended");
    }
    X86ThreadContext context = (X86ThreadContext) thrCtx;
    long[] data = new long[X86ThreadContext.NPRGREG];
    for (int i = 0; i < data.length; i++) {
      data[i] = context.getRegister(i);
    }
    debugger.setThreadIntegerRegisterSet(handle, mustDuplicate, data);
  }
  public boolean equals(Object obj) {
    if ((obj == null) || !(obj instanceof Win32Thread)) {
      return false;
    }
    return (((Win32Thread) obj).getThreadID() == getThreadID());
  }
  public int hashCode() {
    return getThreadID();
  }
  public String toString() {
    return Integer.toString(getThreadID());
  }
  private int getThreadID() {
    if (!gotID) {
      try {
        X86ThreadContext context = (X86ThreadContext) getContext();
        Win32LDTEntry ldt =
          debugger.getThreadSelectorEntry(handle,
                                          mustDuplicate,
                                          (int) context.getRegister(X86ThreadContext.FS));
        Address teb = debugger.newAddress(ldt.getBase());
        id = (int) teb.getCIntegerAt(0x24, 4, true);
        gotID = true;
      } catch (AddressException e) {
        throw new DebuggerException(e);
      }
    }
    return id;
  }
}
