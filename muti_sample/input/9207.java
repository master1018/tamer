class Win32CDebugger implements CDebugger, ProcessControl {
  private Win32Debugger dbg;
  Win32CDebugger(Win32Debugger dbg) {
    this.dbg = dbg;
  }
  public List getThreadList() throws DebuggerException {
    return dbg.getThreadList();
  }
  public List getLoadObjectList() throws DebuggerException{
    return dbg.getLoadObjectList();
  }
  public LoadObject loadObjectContainingPC(Address pc) throws DebuggerException {
    if (pc == null) {
      return null;
    }
    List objs = getLoadObjectList();
    for (Iterator iter = objs.iterator(); iter.hasNext(); ) {
      LoadObject obj = (LoadObject) iter.next();
      if (AddressOps.lte(obj.getBase(), pc) && (pc.minus(obj.getBase()) < obj.getSize())) {
        return obj;
      }
    }
    return null;
  }
  public CFrame topFrameForThread(ThreadProxy thread) throws DebuggerException {
    X86ThreadContext context = (X86ThreadContext) thread.getContext();
    Address ebp = context.getRegisterAsAddress(X86ThreadContext.EBP);
    if (ebp == null) return null;
    Address pc  = context.getRegisterAsAddress(X86ThreadContext.EIP);
    if (pc == null) return null;
    return new X86CFrame(this, ebp, pc);
  }
  public String getNameOfFile(String fileName) {
    return new File(fileName).getName();
  }
  public ProcessControl getProcessControl() throws DebuggerException {
    return this;
  }
  public boolean canDemangle() {
    return false;
  }
  public String demangle(String sym) {
    throw new UnsupportedOperationException();
  }
  public void suspend() throws DebuggerException {
    dbg.suspend();
  }
  public void resume() throws DebuggerException {
    dbg.resume();
  }
  public boolean isSuspended() throws DebuggerException {
    return dbg.isSuspended();
  }
  public void setBreakpoint(Address addr) throws DebuggerException {
    dbg.setBreakpoint(addr);
  }
  public void clearBreakpoint(Address addr) throws DebuggerException {
    dbg.clearBreakpoint(addr);
  }
  public boolean isBreakpointSet(Address addr) throws DebuggerException {
    return dbg.isBreakpointSet(addr);
  }
  public DebugEvent debugEventPoll() throws DebuggerException {
    return dbg.debugEventPoll();
  }
  public void debugEventContinue() throws DebuggerException {
    dbg.debugEventContinue();
  }
}
