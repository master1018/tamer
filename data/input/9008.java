class LinuxCDebugger implements CDebugger {
  private LinuxDebugger dbg;
  LinuxCDebugger(LinuxDebugger dbg) {
    this.dbg = dbg;
  }
  public List getThreadList() throws DebuggerException {
    return dbg.getThreadList();
  }
  public List getLoadObjectList() throws DebuggerException {
    return dbg.getLoadObjectList();
  }
  public LoadObject loadObjectContainingPC(Address pc) throws DebuggerException {
    if (pc == null) {
      return null;
    }
    List objs = getLoadObjectList();
    Object[] arr = objs.toArray();
    int mid  = -1;
    int low  = 0;
    int high = arr.length - 1;
    while (low <= high) {
       mid = (low + high) >> 1;
       LoadObject midVal = (LoadObject) arr[mid];
       long cmp = pc.minus(midVal.getBase());
       if (cmp < 0) {
          high = mid - 1;
       } else if (cmp > 0) {
          long size = midVal.getSize();
          if (cmp >= size) {
             low = mid + 1;
          } else {
             return (LoadObject) arr[mid];
          }
       } else { 
          return (LoadObject) arr[mid];
       }
    }
    return null;
  }
  public CFrame topFrameForThread(ThreadProxy thread) throws DebuggerException {
    String cpu = dbg.getCPU();
    if (cpu.equals("x86")) {
       X86ThreadContext context = (X86ThreadContext) thread.getContext();
       Address ebp = context.getRegisterAsAddress(X86ThreadContext.EBP);
       if (ebp == null) return null;
       Address pc  = context.getRegisterAsAddress(X86ThreadContext.EIP);
       if (pc == null) return null;
       return new LinuxX86CFrame(dbg, ebp, pc);
    } else if (cpu.equals("amd64")) {
       AMD64ThreadContext context = (AMD64ThreadContext) thread.getContext();
       Address rbp = context.getRegisterAsAddress(AMD64ThreadContext.RBP);
       if (rbp == null) return null;
       Address pc  = context.getRegisterAsAddress(AMD64ThreadContext.RIP);
       if (pc == null) return null;
       return new LinuxAMD64CFrame(dbg, rbp, pc);
    } else if (cpu.equals("sparc")) {
       SPARCThreadContext context = (SPARCThreadContext) thread.getContext();
       Address sp = context.getRegisterAsAddress(SPARCThreadContext.R_SP);
       if (sp == null) return null;
       Address pc  = context.getRegisterAsAddress(SPARCThreadContext.R_O7);
       if (pc == null) return null;
       return new LinuxSPARCCFrame(dbg, sp, pc, LinuxDebuggerLocal.getAddressSize());
    } else {
       throw new DebuggerException(cpu + " is not yet supported");
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
