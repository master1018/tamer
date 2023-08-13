class WindbgIA64Thread implements ThreadProxy {
  private WindbgDebugger debugger;
  private long           sysId;
  private boolean        gotID;
  private long           id;
  WindbgIA64Thread(WindbgDebugger debugger, Address addr) {
    this.debugger = debugger;
    this.sysId   = (int) addr.addOffsetTo(debugger.getAddressSize()).getCIntegerAt(0, 4, true);
    gotID = false;
  }
  WindbgIA64Thread(WindbgDebugger debugger, long sysId) {
    this.debugger = debugger;
    this.sysId    = sysId;
    gotID         = false;
  }
  public ThreadContext getContext() throws IllegalThreadStateException {
    long[] data = debugger.getThreadIntegerRegisterSet(getThreadID());
    WindbgIA64ThreadContext context = new WindbgIA64ThreadContext(debugger);
    for (int i = 0; i < data.length; i++) {
      context.setRegister(i, data[i]);
    }
    return context;
  }
  public boolean canSetContext() throws DebuggerException {
    return false;
  }
  public void setContext(ThreadContext thrCtx)
    throws IllegalThreadStateException, DebuggerException {
    throw new DebuggerException("Unimplemented");
  }
  public boolean equals(Object obj) {
    if ((obj == null) || !(obj instanceof WindbgIA64Thread)) {
      return false;
    }
    return (((WindbgIA64Thread) obj).getThreadID() == getThreadID());
  }
  public int hashCode() {
    return (int) getThreadID();
  }
  public String toString() {
    return Long.toString(getThreadID());
  }
  private long getThreadID() {
    if (!gotID) {
       id = debugger.getThreadIdFromSysId(sysId);
    }
    return id;
  }
}
