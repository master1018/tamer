public class DbxDebuggerLocal extends DebuggerBase implements DbxDebugger {
  protected boolean unalignedAccessesOkay;
  protected DbxThreadFactory threadFactory;
  private String dbxPathName;
  private String[] dbxSvcAgentDSOPathNames;
  private Process dbxProcess;
  private StreamMonitor dbxOutStreamMonitor;
  private StreamMonitor dbxErrStreamMonitor;
  private PrintWriter dbxOstr;
  private PrintWriter out;
  private InputLexer in;
  private Socket importModuleSocket;
  private static final int PORT = 21928;
  private static final int  LONG_TIMEOUT = 60000;
  private static final int  DBX_MODULE_NOT_FOUND      = 101;
  private static final int  DBX_MODULE_LOADED         = 102;
  public DbxDebuggerLocal(MachineDescription machDesc,
                          String dbxPathName,
                          String[] dbxSvcAgentDSOPathNames,
                          boolean useCache) {
    this.machDesc = machDesc;
    this.dbxPathName = dbxPathName;
    this.dbxSvcAgentDSOPathNames = dbxSvcAgentDSOPathNames;
    int cacheNumPages;
    int cachePageSize;
    if (PlatformInfo.getCPU().equals("sparc")) {
      cacheNumPages = parseCacheNumPagesProperty(2048);
      cachePageSize = 8192;
      threadFactory = new DbxSPARCThreadFactory(this);
    } else if (PlatformInfo.getCPU().equals("x86")) {
      cacheNumPages = 4096;
      cachePageSize = 4096;
      threadFactory = new DbxX86ThreadFactory(this);
      unalignedAccessesOkay = true;
    } else {
      throw new RuntimeException("Thread access for CPU architecture " + PlatformInfo.getCPU() + " not yet supported");
    }
    if (useCache) {
      initCache(cachePageSize, cacheNumPages);
    }
  }
  protected DbxDebuggerLocal() {
  }
  public boolean hasProcessList() throws DebuggerException {
    return false;
  }
  public List getProcessList() throws DebuggerException {
    throw new DebuggerException("Not yet supported");
  }
  public synchronized void attach(int processID) throws DebuggerException {
    try {
      launchProcess();
      dbxErrStreamMonitor.addTrigger("dbx: no process", 1);
      dbxErrStreamMonitor.addTrigger("dbx: Cannot open", 1);
      dbxErrStreamMonitor.addTrigger("dbx: Cannot find", DBX_MODULE_NOT_FOUND);
      dbxOstr = new PrintWriter(dbxProcess.getOutputStream(), true);
      dbxOstr.println("debug - " + processID);
      dbxOstr.println("kprint -u2 \\(ready\\)");
      boolean seen = dbxErrStreamMonitor.waitFor("(ready)", LONG_TIMEOUT);
      if (!seen) {
        detach();
        throw new DebuggerException("Timed out while connecting to process " + processID);
      }
      List retVals = dbxErrStreamMonitor.getTriggersSeen();
      if (retVals.contains(new Integer(1))) {
        detach();
        throw new DebuggerException("No such process " + processID);
      }
      importDbxModule();
      dbxOstr.println("svc_agent_run");
      connectToImportModule();
      printlnToOutput("peek_fail_fast 1");
    }
    catch (IOException e) {
      detach();
      throw new DebuggerException("Error while connecting to dbx process", e);
    }
  }
  public synchronized void attach(String executableName, String coreFileName) throws DebuggerException {
    try {
      launchProcess();
      dbxErrStreamMonitor.addTrigger("dbx: Cannot open", 1);
      dbxErrStreamMonitor.addTrigger("dbx: can't read", 2);
      dbxErrStreamMonitor.addTrigger("dbx: File", 3);
      dbxErrStreamMonitor.addTrigger("dbx: Unable to read", 4);
      dbxErrStreamMonitor.addTrigger("dbx: core object name", 5);
      dbxErrStreamMonitor.addTrigger("dbx: can't stat", 6);
      dbxOstr = new PrintWriter(dbxProcess.getOutputStream(), true);
      dbxOstr.println("debug " + executableName + " " + coreFileName);
      dbxOstr.println("kprint -u2 \\(ready\\)");
      boolean seen = dbxErrStreamMonitor.waitFor("(ready)", LONG_TIMEOUT);
      if (!seen) {
        detach();
        throw new DebuggerException("Timed out while attaching to core file");
      }
      List retVals = dbxErrStreamMonitor.getTriggersSeen();
      if (retVals.size() > 0) {
        detach();
        if (retVals.contains(new Integer(1))) {
          throw new DebuggerException("Can not find executable \"" + executableName + "\"");
        } else if (retVals.contains(new Integer(2))) {
          throw new DebuggerException("Can not find core file \"" + coreFileName + "\"");
        } else if (retVals.contains(new Integer(3))) {
          throw new DebuggerException("Corrupt executable \"" + executableName + "\"");
        } else if (retVals.contains(new Integer(4))) {
          throw new DebuggerException("Corrupt core file \"" + coreFileName + "\"");
        } else if (retVals.contains(new Integer(5))) {
          throw new DebuggerException("Mismatched core file/executable \"" + coreFileName + "\"/\"" + executableName + "\"");
        } else {
          throw new DebuggerException("Couldn't find all loaded libraries for executable \"" + executableName + "\"");
        }
      }
      importDbxModule();
      dbxOstr.println("svc_agent_run");
      connectToImportModule();
      printlnToOutput("peek_fail_fast 1");
    }
    catch (IOException e) {
      detach();
      throw new DebuggerException("Error while connecting to dbx process", e);
    }
  }
  public synchronized boolean detach() {
    try {
      if (dbxProcess == null) {
        return false;
      }
      if (out != null && dbxOstr != null) {
        printlnToOutput("exit");
        dbxOstr.println("exit");
        try {
          Thread.sleep(500);
        }
        catch (InterruptedException e) {
        }
      }
      shutdown();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
  public Address parseAddress(String addressString) throws NumberFormatException {
    long addr = utils.scanAddress(addressString);
    if (addr == 0) {
      return null;
    }
    return new DbxAddress(this, addr);
  }
  public String getOS() {
    return PlatformInfo.getOS();
  }
  public String getCPU() {
    return PlatformInfo.getCPU();
  }
  public boolean hasConsole() throws DebuggerException {
    return true;
  }
  public synchronized String consoleExecuteCommand(String cmd) throws DebuggerException {
    try {
      printlnToOutput("exit");
      importModuleSocket.close();
      importModuleSocket = null;
      out = null;
      in = null;
      dbxOstr.println("kprint \\(ready\\)");
      dbxOstr.flush();
      dbxOutStreamMonitor.waitFor("(ready)", LONG_TIMEOUT);
      dbxOutStreamMonitor.startCapture();
      dbxErrStreamMonitor.startCapture();
      dbxOstr.println(cmd);
      dbxOstr.println("kprint \\(ready\\)");
      dbxOutStreamMonitor.waitFor("(ready)", LONG_TIMEOUT);
      String result = dbxOutStreamMonitor.stopCapture();
      String result2 = dbxErrStreamMonitor.stopCapture();
      result = result + result2;
      StringBuffer outBuf = new StringBuffer(result.length());
      BufferedReader reader = new BufferedReader(new StringReader(result));
      String line = null;
      do {
        line = reader.readLine();
        if ((line != null) && (!line.equals("(ready)"))) {
          outBuf.append(line);
          outBuf.append("\n");
        }
      } while (line != null);
      dbxOstr.println("svc_agent_run");
      dbxOstr.flush();
      connectToImportModule();
      return outBuf.toString();
    }
    catch (IOException e) {
      detach();
      throw new DebuggerException("Error while executing command on dbx console", e);
    }
  }
  public String getConsolePrompt() throws DebuggerException {
    return "(dbx) ";
  }
  public CDebugger getCDebugger() throws DebuggerException {
    return null;
  }
  public synchronized Address lookup(String objectName, String symbol) {
    long addr = lookupInProcess(objectName, symbol);
    if (addr == 0) {
      return null;
    }
    return new DbxAddress(this, addr);
  }
  public synchronized OopHandle lookupOop(String objectName, String symbol) {
    long addr = lookupInProcess(objectName, symbol);
    if (addr == 0) {
      return null;
    }
    return new DbxOopHandle(this, addr);
  }
  public MachineDescription getMachineDescription() {
    return machDesc;
  }
  public void setMachineDescription(MachineDescription machDesc) {
    this.machDesc = machDesc;
    setBigEndian(machDesc.isBigEndian());
    utils = new DebuggerUtilities(machDesc.getAddressSize(), machDesc.isBigEndian());
  }
  public int getRemoteProcessAddressSize() {
    if (dbxProcess == null) {
      throw new RuntimeException("Not attached to remote process");
    }
    try {
      printlnToOutput("address_size");
      int i = in.parseInt();
      return i;
    }
    catch (IOException e) {
      return -1;
    }
  }
  public ThreadProxy getThreadForIdentifierAddress(Address addr) {
    return threadFactory.createThreadWrapper(addr);
  }
  public ThreadProxy getThreadForThreadId(long id) {
    return threadFactory.createThreadWrapper(id);
  }
  public long readJLong(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    checkJavaConfigured();
    if (unalignedAccessesOkay) {
      utils.checkAlignment(address, jintSize);
    } else {
      utils.checkAlignment(address, jlongSize);
    }
    byte[] data = readBytes(address, jlongSize);
    return utils.dataToJLong(data, jlongSize);
  }
  public String addressValueToString(long address) {
    return utils.addressValueToString(address);
  }
  public long readCInteger(long address, long numBytes, boolean isUnsigned)
    throws UnmappedAddressException, UnalignedAddressException {
    checkConfigured();
    if (!unalignedAccessesOkay) {
      utils.checkAlignment(address, numBytes);
    } else {
      if (numBytes == 8) {
        utils.checkAlignment(address, 4);
      } else {
        utils.checkAlignment(address, numBytes);
      }
    }
    byte[] data = readBytes(address, numBytes);
    return utils.dataToCInteger(data, isUnsigned);
  }
  public DbxAddress readAddress(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    long value = readAddressValue(address);
    return (value == 0 ? null : new DbxAddress(this, value));
  }
  public DbxAddress readCompOopAddress(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    long value = readCompOopAddressValue(address);
    return (value == 0 ? null : new DbxAddress(this, value));
  }
  public DbxOopHandle readOopHandle(long address)
    throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
    long value = readAddressValue(address);
    return (value == 0 ? null : new DbxOopHandle(this, value));
  }
  public DbxOopHandle readCompOopHandle(long address)
    throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
    long value = readCompOopAddressValue(address);
    return (value == 0 ? null : new DbxOopHandle(this, value));
  }
  public synchronized long[] getThreadIntegerRegisterSet(int tid) {
    try {
      printlnToOutput("thr_gregs " + tid);
      int num = in.parseInt();
      long[] res = new long[num];
      for (int i = 0; i < num; i++) {
        res[i] = in.parseAddress();
      }
      return res;
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  public long getAddressValue(Address addr) {
    if (addr == null) return 0;
    return ((DbxAddress) addr).getValue();
  }
  public Address newAddress(long value) {
    if (value == 0) return null;
    return new DbxAddress(this, value);
  }
  private void launchProcess() throws IOException {
    dbxProcess = Runtime.getRuntime().exec(dbxPathName);
    dbxOutStreamMonitor = new StreamMonitor(dbxProcess.getInputStream(), "dbx stdout", true);
    dbxErrStreamMonitor = new StreamMonitor(dbxProcess.getErrorStream(), "dbx stderr", true);
  }
  private void importDbxModule() throws DebuggerException {
    dbxOutStreamMonitor.addTrigger("Defining svc_agent_run", DBX_MODULE_LOADED);
    for (int i = 0; i < dbxSvcAgentDSOPathNames.length; i++) {
      dbxOstr.println("import " + dbxSvcAgentDSOPathNames[i]);
      dbxOstr.println("kprint -u2 \\(Ready\\)");
      boolean seen = dbxErrStreamMonitor.waitFor("(Ready)", LONG_TIMEOUT);
      if (!seen) {
        detach();
        throw new DebuggerException("Timed out while importing dbx module from file\n" + dbxSvcAgentDSOPathNames[i]);
      }
      List retVals = dbxErrStreamMonitor.getTriggersSeen();
      if (retVals.contains(new Integer(DBX_MODULE_NOT_FOUND))) {
        detach();
        throw new DebuggerException("Unable to find the Serviceability Agent's dbx import module at pathname \"" +
                                    dbxSvcAgentDSOPathNames[i] + "\"");
      } else {
        retVals = dbxOutStreamMonitor.getTriggersSeen();
        if (retVals.contains(new Integer(DBX_MODULE_LOADED))) {
          System.out.println("importDbxModule: imported " +  dbxSvcAgentDSOPathNames[i]);
          return;
        }
      }
    }
    detach();
    String errMsg = ("Unable to find a version of the Serviceability Agent's dbx import module\n" +
                     "matching the architecture of dbx at any of the following locations:");
    for (int i = 0; i < dbxSvcAgentDSOPathNames.length; i++) {
      errMsg = errMsg + "\n" + dbxSvcAgentDSOPathNames[i];
    }
    throw new DebuggerException(errMsg);
  }
  private void shutdown() {
    if (dbxProcess != null) {
      try {
        dbxProcess.exitValue();
      }
      catch (IllegalThreadStateException e) {
        dbxProcess.destroy();
      }
    }
    try {
      if (importModuleSocket != null) {
        importModuleSocket.close();
      }
    }
    catch (IOException e) {
    }
    clear();
    clearCache();
  }
  synchronized long lookupInProcess(String objectName, String symbol) {
    try {
      printlnToOutput("lookup " + objectName + " " + symbol);
      return in.parseAddress();
    }
    catch (Exception e) {
      return 0;
    }
  }
  public synchronized ReadResult readBytesFromProcess(long address, long numBytes)
    throws DebuggerException {
    if (numBytes < 0) {
      throw new DebuggerException("Can not read negative number (" + numBytes + ") of bytes from process");
    }
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
      int numReads = 0;
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
        ++numReads;
      }
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(numBytes == 0, "Bug in debug server's implementation of peek: numBytesLeft == " +
                    numBytes + ", should be 0 (did " + numReads + " reads)");
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
  public void writeBytesToProcess(long address, long numBytes, byte[] data)
    throws UnmappedAddressException, DebuggerException {
    throw new DebuggerException("Unimplemented");
  }
  ReadResult readBytesFromProcessInternal(long address, long numBytes)
    throws DebuggerException {
    return readBytesFromProcess(address, numBytes);
  }
  private void printlnToOutput(String s) throws IOException {
    out.println(s);
    if (out.checkError()) {
      throw new IOException("Error occurred while writing to debug server");
    }
  }
  private void clear() {
    dbxProcess = null;
    dbxOstr = null;
    out = null;
    in = null;
    importModuleSocket = null;
  }
  private void connectToImportModule() throws IOException {
    importModuleSocket = null;
    long endTime = System.currentTimeMillis() + LONG_TIMEOUT;
    while ((importModuleSocket == null) && (System.currentTimeMillis() < endTime)) {
      try {
        importModuleSocket = new Socket(InetAddress.getLocalHost(), PORT);
        importModuleSocket.setTcpNoDelay(true);
      }
      catch (IOException e) {
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException ex) {
        }
      }
    }
    if (importModuleSocket == null) {
      detach();
      throw new DebuggerException("Timed out while attempting to connect to remote dbx process");
    }
    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(importModuleSocket.getOutputStream(), "US-ASCII")), true);
    in = new InputLexer(new BufferedInputStream(importModuleSocket.getInputStream()));
  }
}
