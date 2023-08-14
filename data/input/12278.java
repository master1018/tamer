class WindbgCDebugger implements CDebugger {
  private WindbgDebugger dbg;
  WindbgCDebugger(WindbgDebugger dbg) {
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
    if (dbg.getCPU().equals("x86")) {
      X86ThreadContext context = (X86ThreadContext) thread.getContext();
      Address ebp = context.getRegisterAsAddress(X86ThreadContext.EBP);
      if (ebp == null) return null;
      Address pc  = context.getRegisterAsAddress(X86ThreadContext.EIP);
      if (pc == null) return null;
      return new X86CFrame(this, ebp, pc);
    } else if (dbg.getCPU().equals("amd64")) {
      AMD64ThreadContext context = (AMD64ThreadContext) thread.getContext();
      Address rbp = context.getRegisterAsAddress(AMD64ThreadContext.RBP);
      if (rbp == null) return null;
      Address pc  = context.getRegisterAsAddress(AMD64ThreadContext.RIP);
      if (pc == null) return null;
      return new AMD64CFrame(this, rbp, pc);
    } else {
      return null;
    }
  }
  public String getNameOfFile(String fileName) {
    return new File(fileName).getName();
  }
  public ProcessControl getProcessControl() throws DebuggerException {
    return null;
  }
  public boolean canDemangle() {
    return false;
  }
  public String demangle(String sym) {
    throw new UnsupportedOperationException();
  }
}
