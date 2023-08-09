public class RemoteDebuggerServer extends UnicastRemoteObject
  implements RemoteDebugger {
  private transient Debugger debugger;
  public RemoteDebuggerServer() throws RemoteException {
    super();
  }
  public RemoteDebuggerServer(Debugger debugger) throws RemoteException {
    super();
    this.debugger = debugger;
  }
  public String getOS() throws RemoteException {
    return debugger.getOS();
  }
  public String getCPU() throws RemoteException {
    return debugger.getCPU();
  }
  public MachineDescription getMachineDescription() throws RemoteException {
    return debugger.getMachineDescription();
  }
  public long lookupInProcess(String objectName, String symbol) throws RemoteException {
    Address addr = debugger.lookup(objectName, symbol);
    return addr == null? 0L : debugger.getAddressValue(addr);
  }
  public ReadResult readBytesFromProcess(long address, long numBytes) throws RemoteException {
    return debugger.readBytesFromProcess(address, numBytes);
  }
  public boolean hasConsole() throws RemoteException {
    return debugger.hasConsole();
  }
  public String getConsolePrompt() throws RemoteException {
    return debugger.getConsolePrompt();
  }
  public String consoleExecuteCommand(String cmd) throws RemoteException {
    return debugger.consoleExecuteCommand(cmd);
  }
  public long getJBooleanSize() throws RemoteException {
    return debugger.getJBooleanSize();
  }
  public long getJByteSize() throws RemoteException {
    return debugger.getJByteSize();
  }
  public long getJCharSize() throws RemoteException {
    return debugger.getJCharSize();
  }
  public long getJDoubleSize() throws RemoteException {
    return debugger.getJDoubleSize();
  }
  public long getJFloatSize() throws RemoteException {
    return debugger.getJFloatSize();
  }
  public long getJIntSize() throws RemoteException {
    return debugger.getJIntSize();
  }
  public long getJLongSize() throws RemoteException {
    return debugger.getJLongSize();
  }
  public long getJShortSize() throws RemoteException {
    return debugger.getJShortSize();
  }
  public long getHeapOopSize() throws RemoteException {
    return debugger.getHeapOopSize();
  }
  public long getNarrowOopBase() throws RemoteException {
    return debugger.getNarrowOopBase();
  }
  public int  getNarrowOopShift() throws RemoteException {
    return debugger.getNarrowOopShift();
  }
  public boolean   areThreadsEqual(long addrOrId1, boolean isAddress1,
                                   long addrOrId2, boolean isAddress2) throws RemoteException {
    ThreadProxy t1 = getThreadProxy(addrOrId1, isAddress1);
    ThreadProxy t2 = getThreadProxy(addrOrId2, isAddress2);
    return t1.equals(t2);
  }
  public int       getThreadHashCode(long addrOrId, boolean isAddress) throws RemoteException {
    ThreadProxy t = getThreadProxy(addrOrId, isAddress);
    return t.hashCode();
  }
  public long[]    getThreadIntegerRegisterSet(long addrOrId, boolean isAddress) throws RemoteException {
    ThreadProxy t = getThreadProxy(addrOrId, isAddress);
    ThreadContext tc = t.getContext();
    long[] regs = new long[tc.getNumRegisters()];
    for (int r = 0; r < regs.length; r++) {
       regs[r] = tc.getRegister(r);
    }
    return regs;
  }
  private ThreadProxy getThreadProxy(long addrOrId, boolean isAddress) throws DebuggerException {
     if (isAddress) {
        Address addr = debugger.parseAddress("0x" + Long.toHexString(addrOrId));
        return debugger.getThreadForIdentifierAddress(addr);
     } else {
        return debugger.getThreadForThreadId(addrOrId);
     }
  }
}
