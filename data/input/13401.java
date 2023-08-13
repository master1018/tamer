class ProcCDebugger implements CDebugger {
  private ProcDebugger dbg;
  ProcCDebugger(ProcDebugger dbg) {
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
    return dbg.topFrameForThread(thread);
  }
  public String getNameOfFile(String fileName) {
    return new File(fileName).getName();
  }
  public ProcessControl getProcessControl() throws DebuggerException {
    return null;
  }
  public boolean canDemangle() {
    return true;
  }
  public String demangle(String sym) {
    return dbg.demangle(sym);
  }
}
