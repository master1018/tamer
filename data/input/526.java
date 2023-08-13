public class Win32DebuggerLocal extends DebuggerBase implements Win32Debugger {
  private Socket debuggerSocket;
  private boolean attached;
  private long pid;
  private PrintWriter out;
  private DataOutputStream rawOut;
  private InputLexer in;
  private static final int PORT = 27000;
  private PageCache cache;
  private static final long SHORT_TIMEOUT = 2000;
  private static final long LONG_TIMEOUT = 20000;
  private Map nameToDllMap;
  private List loadObjects;
  private CDebugger cdbg;
  private boolean suspended;
  private Map     breakpoints;
  private DebugEvent curDebugEvent;
  public Win32DebuggerLocal(MachineDescription machDesc,
                            boolean useCache) throws DebuggerException {
    this.machDesc = machDesc;
    utils = new DebuggerUtilities(machDesc.getAddressSize(), machDesc.isBigEndian());
    if (useCache) {
      initCache(4096, parseCacheNumPagesProperty(4096));
    }
    try {
      connectToDebugServer();
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public boolean hasProcessList() throws DebuggerException {
    return true;
  }
  public List getProcessList() throws DebuggerException {
    List processes = new ArrayList();
    try {
      printlnToOutput("proclist");
      int num = in.parseInt();
      for (int i = 0; i < num; i++) {
        int pid = in.parseInt();
        String name = parseString();
        if (name.equals("")) {
          name = "System Idle Process";
        }
        processes.add(new ProcessInfo(name, pid));
      }
      return processes;
    }
    catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public synchronized void attach(int processID) throws DebuggerException {
    if (attached) {
      throw new DebuggerException("Already attached to process " + pid);
    }
    try {
      printlnToOutput("attach " + processID);
      if (!in.parseBoolean()) {
        throw new DebuggerException("Error attaching to process, or no such process");
      }
      attached = true;
      pid = processID;
      suspended = true;
      breakpoints = new HashMap();
      curDebugEvent = null;
      nameToDllMap = null;
      loadObjects = null;
    }
    catch (IOException e) {
        throw new DebuggerException(e);
    }
  }
  public synchronized void attach(String executableName, String coreFileName) throws DebuggerException {
    throw new DebuggerException("Core files not yet supported on Win32");
  }
  public synchronized boolean detach() {
    if (!attached) {
      return false;
    }
    attached = false;
    suspended = false;
    breakpoints = null;
    if (nameToDllMap != null) {
      for (Iterator iter = nameToDllMap.values().iterator(); iter.hasNext(); ) {
        DLL dll = (DLL) iter.next();
        dll.close();
      }
      nameToDllMap = null;
      loadObjects = null;
    }
    cdbg = null;
    clearCache();
    try {
      printlnToOutput("detach");
      return in.parseBoolean();
    }
    catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public Address parseAddress(String addressString) throws NumberFormatException {
    return newAddress(utils.scanAddress(addressString));
  }
  public String getOS() {
    return PlatformInfo.getOS();
  }
  public String getCPU() {
    return PlatformInfo.getCPU();
  }
  public boolean hasConsole() throws DebuggerException {
    return false;
  }
  public String consoleExecuteCommand(String cmd) throws DebuggerException {
    throw new DebuggerException("No debugger console available on Win32");
  }
  public String getConsolePrompt() throws DebuggerException {
    return null;
  }
  public CDebugger getCDebugger() throws DebuggerException {
    if (cdbg == null) {
      cdbg = new Win32CDebugger(this);
    }
    return cdbg;
  }
  public synchronized Address lookup(String objectName, String symbol) {
    if (!attached) {
      return null;
    }
    return newAddress(lookupInProcess(objectName, symbol));
  }
  public synchronized OopHandle lookupOop(String objectName, String symbol) {
    Address addr = lookup(objectName, symbol);
    if (addr == null) {
      return null;
    }
    return addr.addOffsetToAsOopHandle(0);
  }
  public MachineDescription getMachineDescription() {
    return machDesc;
  }
  public ThreadProxy getThreadForIdentifierAddress(Address addr) {
    return new Win32Thread(this, addr);
  }
  public ThreadProxy getThreadForThreadId(long handle) {
    return new Win32Thread(this, handle);
  }
  public long readJLong(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    utils.checkAlignment(address, jintSize);
    byte[] data = readBytes(address, jlongSize);
    return utils.dataToJLong(data, jlongSize);
  }
  public String addressValueToString(long address) {
    return utils.addressValueToString(address);
  }
  public Win32Address readAddress(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    return (Win32Address) newAddress(readAddressValue(address));
  }
  public Win32Address readCompOopAddress(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    return (Win32Address) newAddress(readCompOopAddressValue(address));
  }
  public Win32OopHandle readOopHandle(long address)
    throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
    long value = readAddressValue(address);
    return (value == 0 ? null : new Win32OopHandle(this, value));
  }
  public Win32OopHandle readCompOopHandle(long address)
    throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
    long value = readCompOopAddressValue(address);
    return (value == 0 ? null : new Win32OopHandle(this, value));
  }
  public void writeAddress(long address, Win32Address value) {
    writeAddressValue(address, getAddressValue(value));
  }
  public void writeOopHandle(long address, Win32OopHandle value) {
    writeAddressValue(address, getAddressValue(value));
  }
  public synchronized long[] getThreadIntegerRegisterSet(int threadHandleValue,
                                                         boolean mustDuplicateHandle)
    throws DebuggerException {
    if (!suspended) {
      throw new DebuggerException("Process not suspended");
    }
    try {
      int handle = threadHandleValue;
      if (mustDuplicateHandle) {
        printlnToOutput("duphandle 0x" + Integer.toHexString(threadHandleValue));
        if (!in.parseBoolean()) {
          throw new DebuggerException("Error duplicating thread handle 0x" + threadHandleValue);
        }
        handle = (int) in.parseAddress(); 
      }
      printlnToOutput("getcontext 0x" + Integer.toHexString(handle));
      if (!in.parseBoolean()) {
        if (mustDuplicateHandle) {
          printlnToOutput("closehandle 0x" + Integer.toHexString(handle));
        }
        String failMessage = "GetThreadContext failed for thread handle 0x" +
                             Integer.toHexString(handle);
        if (mustDuplicateHandle) {
          failMessage = failMessage + ", duplicated from thread handle " +
                        Integer.toHexString(threadHandleValue);
        }
        throw new DebuggerException(failMessage);
      }
      int numRegs = 22;
      long[] winRegs = new long[numRegs];
      for (int i = 0; i < numRegs; i++) {
        winRegs[i] = in.parseAddress();
      }
      if (mustDuplicateHandle) {
        printlnToOutput("closehandle 0x" + Integer.toHexString(handle));
      }
      long[] retval = new long[X86ThreadContext.NPRGREG];
      retval[X86ThreadContext.EAX] = winRegs[0];
      retval[X86ThreadContext.EBX] = winRegs[1];
      retval[X86ThreadContext.ECX] = winRegs[2];
      retval[X86ThreadContext.EDX] = winRegs[3];
      retval[X86ThreadContext.ESI] = winRegs[4];
      retval[X86ThreadContext.EDI] = winRegs[5];
      retval[X86ThreadContext.EBP] = winRegs[6];
      retval[X86ThreadContext.ESP] = winRegs[7];
      retval[X86ThreadContext.EIP] = winRegs[8];
      retval[X86ThreadContext.DS]  = winRegs[9];
      retval[X86ThreadContext.ES]  = winRegs[10];
      retval[X86ThreadContext.FS]  = winRegs[11];
      retval[X86ThreadContext.GS]  = winRegs[12];
      retval[X86ThreadContext.CS]  = winRegs[13];
      retval[X86ThreadContext.SS]  = winRegs[14];
      retval[X86ThreadContext.EFL] = winRegs[15];
      retval[X86ThreadContext.DR0] = winRegs[16];
      retval[X86ThreadContext.DR1] = winRegs[17];
      retval[X86ThreadContext.DR2] = winRegs[18];
      retval[X86ThreadContext.DR3] = winRegs[19];
      retval[X86ThreadContext.DR6] = winRegs[20];
      retval[X86ThreadContext.DR7] = winRegs[21];
      return retval;
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public synchronized void setThreadIntegerRegisterSet(int threadHandleValue,
                                                       boolean mustDuplicateHandle,
                                                       long[] context)
    throws DebuggerException {
    if (!suspended) {
      throw new DebuggerException("Process not suspended");
    }
    try {
      int handle = threadHandleValue;
      if (mustDuplicateHandle) {
        printlnToOutput("duphandle 0x" + Integer.toHexString(threadHandleValue));
        if (!in.parseBoolean()) {
          throw new DebuggerException("Error duplicating thread handle 0x" + threadHandleValue);
        }
        handle = (int) in.parseAddress(); 
      }
      long[] winRegs = new long[context.length];
      winRegs[0] = context[X86ThreadContext.EAX];
      winRegs[1] = context[X86ThreadContext.EBX];
      winRegs[2] = context[X86ThreadContext.ECX];
      winRegs[3] = context[X86ThreadContext.EDX];
      winRegs[4] = context[X86ThreadContext.ESI];
      winRegs[5] = context[X86ThreadContext.EDI];
      winRegs[6] = context[X86ThreadContext.EBP];
      winRegs[7] = context[X86ThreadContext.ESP];
      winRegs[8] = context[X86ThreadContext.EIP];
      winRegs[9] = context[X86ThreadContext.DS];
      winRegs[10] = context[X86ThreadContext.ES];
      winRegs[11] = context[X86ThreadContext.FS];
      winRegs[12] = context[X86ThreadContext.GS];
      winRegs[13] = context[X86ThreadContext.CS];
      winRegs[14] = context[X86ThreadContext.SS];
      winRegs[15] = context[X86ThreadContext.EFL];
      winRegs[16] = context[X86ThreadContext.DR0];
      winRegs[17] = context[X86ThreadContext.DR1];
      winRegs[18] = context[X86ThreadContext.DR2];
      winRegs[19] = context[X86ThreadContext.DR3];
      winRegs[20] = context[X86ThreadContext.DR6];
      winRegs[21] = context[X86ThreadContext.DR7];
      StringBuffer cmd = new StringBuffer();
      cmd.append("setcontext 0x");
      cmd.append(Integer.toHexString(threadHandleValue));
      for (int i = 0; i < context.length; i++) {
        cmd.append(" 0x");
        cmd.append(Long.toHexString(winRegs[i]));
      }
      printlnToOutput(cmd.toString());
      boolean res = in.parseBoolean();
      if (mustDuplicateHandle) {
        printlnToOutput("closehandle 0x" + Integer.toHexString(handle));
      }
      if (!res) {
        String failMessage = "SetThreadContext failed for thread handle 0x" +
          Integer.toHexString(handle);
        if (mustDuplicateHandle) {
          failMessage = failMessage + ", duplicated from thread handle " +
            Integer.toHexString(threadHandleValue);
        }
        throw new DebuggerException(failMessage);
      }
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public synchronized Win32LDTEntry getThreadSelectorEntry(int threadHandleValue,
                                                           boolean mustDuplicateHandle,
                                                           int selector)
    throws DebuggerException {
    try {
      int handle = threadHandleValue;
      if (mustDuplicateHandle) {
        printlnToOutput("duphandle 0x" + Integer.toHexString(threadHandleValue));
        if (!in.parseBoolean()) {
          throw new DebuggerException("Error duplicating thread handle 0x" + threadHandleValue);
        }
        handle = (int) in.parseAddress(); 
      }
      printlnToOutput("selectorentry 0x" + Integer.toHexString(handle) + " " + selector);
      if (!in.parseBoolean()) {
        if (mustDuplicateHandle) {
          printlnToOutput("closehandle 0x" + Integer.toHexString(handle));
        }
        throw new DebuggerException("GetThreadContext failed for thread handle 0x" + handle +
                                    ", duplicated from thread handle " + threadHandleValue);
      }
      short limitLow = (short) in.parseAddress();
      short baseLow  = (short) in.parseAddress();
      byte  baseMid  = (byte)  in.parseAddress();
      byte  flags1   = (byte)  in.parseAddress();
      byte  flags2   = (byte)  in.parseAddress();
      byte  baseHi   = (byte)  in.parseAddress();
      return new Win32LDTEntry(limitLow, baseLow, baseMid, flags1, flags2, baseHi);
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public synchronized List getThreadList() throws DebuggerException {
    if (!suspended) {
      throw new DebuggerException("Process not suspended");
    }
    try {
      printlnToOutput("threadlist");
      List ret = new ArrayList();
      int numThreads = in.parseInt();
      for (int i = 0; i < numThreads; i++) {
        int handle = (int) in.parseAddress();
        ret.add(new Win32Thread(this, handle));
      }
      return ret;
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public synchronized List getLoadObjectList() throws DebuggerException {
    if (!suspended) {
      throw new DebuggerException("Process not suspended");
    }
    try {
      if (loadObjects == null) {
        loadObjects  = new ArrayList();
        nameToDllMap = new HashMap();
        printlnToOutput("libinfo");
        int numInfo = in.parseInt();
        for (int i = 0; i < numInfo; i++) {
          String  fullPathName = parseString().toLowerCase();
          Address base         = newAddress(in.parseAddress());
          File   file = new File(fullPathName);
          long   size = file.length();
          DLL    dll  = new DLL(this, fullPathName, size, base);
          String name = file.getName();
          nameToDllMap.put(name, dll);
          loadObjects.add(dll);
        }
      }
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
    return loadObjects;
  }
  public synchronized void writeBytesToProcess(long startAddress, long numBytes, byte[] data)
    throws UnmappedAddressException, DebuggerException {
    try {
      printToOutput("poke 0x" + Long.toHexString(startAddress) +
                    " |");
      writeIntToOutput((int) numBytes);
      writeToOutput(data, 0, (int) numBytes);
      printlnToOutput("");
      if (!in.parseBoolean()) {
        throw new UnmappedAddressException(startAddress);
      }
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public synchronized void suspend() throws DebuggerException {
    try {
      if (suspended) {
        throw new DebuggerException("Process already suspended");
      }
      printlnToOutput("suspend");
      suspended = true;
      enableCache();
      reresolveLoadObjects();
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public synchronized void resume() throws DebuggerException {
    try {
      if (!suspended) {
        throw new DebuggerException("Process not suspended");
      }
      disableCache();
      printlnToOutput("resume");
      suspended = false;
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public synchronized boolean isSuspended() throws DebuggerException {
    return suspended;
  }
  public synchronized void setBreakpoint(Address addr) throws DebuggerException {
    if (!suspended) {
      throw new DebuggerException("Process not suspended");
    }
    long addrVal = getAddressValue(addr);
    Long where = new Long(addrVal);
    if (breakpoints.get(where) != null) {
      throw new DebuggerException("Breakpoint already set at " + addr);
    }
    Byte what = new Byte(readBytes(addrVal, 1)[0]);
    writeBytesToProcess(addrVal, 1, new byte[] { (byte) 0xCC });
    breakpoints.put(where, what);
  }
  public synchronized void clearBreakpoint(Address addr) throws DebuggerException {
    if (!suspended) {
      throw new DebuggerException("Process not suspended");
    }
    long addrVal = getAddressValue(addr);
    Long where = new Long(addrVal);
    Byte what = (Byte) breakpoints.get(where);
    if (what == null) {
      throw new DebuggerException("Breakpoint not set at " + addr);
    }
    writeBytesToProcess(addrVal, 1, new byte[] { what.byteValue() });
    breakpoints.remove(where);
  }
  public synchronized boolean isBreakpointSet(Address addr) throws DebuggerException {
    return (breakpoints.get(new Long(getAddressValue(addr))) != null);
  }
  private static final int EXCEPTION_DEBUG_EVENT  = 1;
  private static final int LOAD_DLL_DEBUG_EVENT   = 6;
  private static final int UNLOAD_DLL_DEBUG_EVENT = 7;
  private static final int EXCEPTION_ACCESS_VIOLATION = 0xC0000005;
  private static final int EXCEPTION_BREAKPOINT       = 0x80000003;
  private static final int EXCEPTION_SINGLE_STEP      = 0x80000004;
  public synchronized DebugEvent debugEventPoll() throws DebuggerException {
    if (curDebugEvent != null) {
      return curDebugEvent;
    }
    try {
      printlnToOutput("pollevent");
      if (!in.parseBoolean()) {
        return null;
      }
      int handle = (int) in.parseAddress();
      ThreadProxy thread = new Win32Thread(this, handle);
      int code = in.parseInt();
      DebugEvent ev = null;
      switch (code) {
      case LOAD_DLL_DEBUG_EVENT: {
        Address addr = newAddress(in.parseAddress());
        ev = BasicDebugEvent.newLoadObjectLoadEvent(thread, addr);
        break;
      }
      case UNLOAD_DLL_DEBUG_EVENT: {
        Address addr = newAddress(in.parseAddress());
        ev = BasicDebugEvent.newLoadObjectUnloadEvent(thread, addr);
        break;
      }
      case EXCEPTION_DEBUG_EVENT: {
        int exceptionCode = in.parseInt();
        Address pc = newAddress(in.parseAddress());
        switch (exceptionCode) {
        case EXCEPTION_ACCESS_VIOLATION:
          boolean wasWrite = in.parseBoolean();
          Address addr = newAddress(in.parseAddress());
          ev = BasicDebugEvent.newAccessViolationEvent(thread, pc, wasWrite, addr);
          break;
        case EXCEPTION_BREAKPOINT:
          ev = BasicDebugEvent.newBreakpointEvent(thread, pc);
          break;
        case EXCEPTION_SINGLE_STEP:
          ev = BasicDebugEvent.newSingleStepEvent(thread, pc);
          break;
        default:
          ev = BasicDebugEvent.newUnknownEvent(thread,
                                               "Exception 0x" + Integer.toHexString(exceptionCode) +
                                               " at PC " + pc);
          break;
        }
        break;
      }
      default:
        ev = BasicDebugEvent.newUnknownEvent(thread,
                                             "Debug event " + code + " occurred");
        break;
      }
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(ev != null, "Must have created event");
      }
      curDebugEvent = ev;
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
    return curDebugEvent;
  }
  public synchronized void debugEventContinue() throws DebuggerException {
    if (curDebugEvent == null) {
      throw new DebuggerException("No debug event pending");
    }
    try {
      DebugEvent.Type t = curDebugEvent.getType();
      boolean shouldPassOn = true;
      if (t == DebugEvent.Type.BREAKPOINT) {
        if (breakpoints.get(new Long(getAddressValue(curDebugEvent.getPC()))) != null) {
          System.err.println("Backing up PC due to breakpoint");
          X86ThreadContext ctx = (X86ThreadContext) curDebugEvent.getThread().getContext();
          ctx.setRegister(X86ThreadContext.EIP, ctx.getRegister(X86ThreadContext.EIP) - 1);
          curDebugEvent.getThread().setContext(ctx);
        } else {
          System.err.println("Skipping back up of PC since I didn't know about this breakpoint");
          System.err.println("Known breakpoints:");
          for (Iterator iter = breakpoints.keySet().iterator(); iter.hasNext(); ) {
            System.err.println("  0x" + Long.toHexString(((Long) iter.next()).longValue()));
          }
        }
        shouldPassOn = false;
      } else if (t == DebugEvent.Type.SINGLE_STEP) {
        shouldPassOn = false;
      }
      int val = (shouldPassOn ? 1 : 0);
      printlnToOutput("continueevent " + val);
      if (!in.parseBoolean()) {
        throw new DebuggerException("Unknown error while attempting to continue past debug event");
      }
      curDebugEvent = null;
    } catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  public long getAddressValue(Address addr) {
    if (addr == null) return 0;
    return ((Win32Address) addr).getValue();
  }
  public Address newAddress(long value) {
    if (value == 0) return null;
    return new Win32Address(this, value);
  }
  private String parseString() throws IOException {
    int charSize = in.parseInt();
    int numChars = in.parseInt();
    in.skipByte();
    String str;
    if (charSize == 1) {
      str = in.readByteString(numChars);
    } else {
      str = in.readCharString(numChars);
    }
    return str;
  }
  synchronized long lookupInProcess(String objectName, String symbol) {
    if (nameToDllMap == null) {
      getLoadObjectList();
    }
    DLL dll = (DLL) nameToDllMap.get(objectName);
    if (dll != null) {
      Win32Address addr = (Win32Address) dll.lookupSymbol(symbol);
      if (addr != null) {
        return addr.getValue();
      }
    }
    return 0;
  }
  public synchronized ReadResult readBytesFromProcess(long address, long numBytes)
    throws UnmappedAddressException, DebuggerException {
    try {
      String cmd = "peek " + utils.addressValueToString(address) + " " + numBytes;
      printlnToOutput(cmd);
      while (in.readByte() != 'B') {
      }
      byte res = in.readByte();
      if (res == 0) {
        System.err.println("Failing command: " + cmd);
        throw new DebuggerException("Read of remote process address space failed");
      }
      byte[] buf = new byte[(int) numBytes];
      boolean bailOut = false;
      long failureAddress = 0;
      while (numBytes > 0) {
        long len = in.readUnsignedInt();
        boolean isMapped = ((in.readByte() == 0) ? false : true);
        if (!isMapped) {
          if (!bailOut) {
            bailOut = true;
            failureAddress = address;
          }
        } else {
          in.readBytes(buf, 0, (int) len);
        }
        numBytes -= len;
        address += len;
      }
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(numBytes == 0, "Bug in debug server's implementation of peek");
      }
      if (bailOut) {
        return new ReadResult(failureAddress);
      }
      return new ReadResult(buf);
    }
    catch (IOException e) {
      throw new DebuggerException(e);
    }
  }
  private void printlnToOutput(String s) throws IOException {
    out.println(s);
    if (out.checkError()) {
      throw new IOException("Error occurred while writing to debug server");
    }
  }
  private void printToOutput(String s) throws IOException {
    out.print(s);
    if (out.checkError()) {
      throw new IOException("Error occurred while writing to debug server");
    }
  }
  private void writeIntToOutput(int val) throws IOException {
    rawOut.writeInt(val);
    rawOut.flush();
  }
  private void writeToOutput(byte[] buf, int off, int len) throws IOException {
    rawOut.write(buf, off, len);
    rawOut.flush();
  }
  private void connectToDebugServer() throws IOException {
    debuggerSocket = null;
    long endTime = System.currentTimeMillis() + SHORT_TIMEOUT;
    while ((debuggerSocket == null) && (System.currentTimeMillis() < endTime)) {
      try {
        debuggerSocket = new Socket(InetAddress.getByName("127.0.0.1"), PORT);
        debuggerSocket.setTcpNoDelay(true);
      }
      catch (IOException e) {
        debuggerSocket = null;
        try {
          Thread.sleep(750);
        }
        catch (InterruptedException ex) {
        }
      }
    }
    if (debuggerSocket == null) {
      throw new DebuggerException("Timed out while attempting to connect to debug server (please start SwDbgSrv.exe)");
    }
    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(debuggerSocket.getOutputStream(), "US-ASCII")), true);
    rawOut = new DataOutputStream(new BufferedOutputStream(debuggerSocket.getOutputStream()));
    in = new InputLexer(new BufferedInputStream(debuggerSocket.getInputStream()));
  }
  private DLL findDLLByName(String fullPathName) {
    for (Iterator iter = loadObjects.iterator(); iter.hasNext(); ) {
      DLL dll = (DLL) iter.next();
      if (dll.getName().equals(fullPathName)) {
        return dll;
      }
    }
    return null;
  }
  private void reresolveLoadObjects() throws DebuggerException {
    try {
      if (loadObjects == null) {
        return;
      }
      List newLoadObjects = new ArrayList();
      printlnToOutput("libinfo");
      int numInfo = in.parseInt();
      for (int i = 0; i < numInfo; i++) {
        String  fullPathName = parseString().toLowerCase();
        Address base         = newAddress(in.parseAddress());
        DLL dll = findDLLByName(fullPathName);
        boolean mustLoad = true;
        if (dll != null) {
          loadObjects.remove(dll);
          if (AddressOps.equal(base, dll.getBase())) {
            mustLoad = false;
          }
        }
        if (mustLoad) {
          File   file = new File(fullPathName);
          long   size = file.length();
          String name = file.getName();
          dll  = new DLL(this, fullPathName, size, base);
          nameToDllMap.put(name, dll);
        }
        newLoadObjects.add(dll);
      }
      for (Iterator dllIter = loadObjects.iterator(); dllIter.hasNext(); ) {
        DLL dll = (DLL) dllIter.next();
        for (Iterator iter = nameToDllMap.keySet().iterator(); iter.hasNext(); ) {
          String name = (String) iter.next();
          if (nameToDllMap.get(name) == dll) {
            nameToDllMap.remove(name);
            break;
          }
        }
      }
      loadObjects = newLoadObjects;
    } catch (IOException e) {
      loadObjects = null;
      nameToDllMap = null;
      throw new DebuggerException(e);
    }
  }
}
