public class LinuxDebuggerLocal extends DebuggerBase implements LinuxDebugger {
    private boolean useGCC32ABI;
    private boolean attached;
    private long    p_ps_prochandle; 
    private boolean isCore;
    private LinuxCDebugger cdbg;
    private List threadList;
    private List loadObjectList;
    private ClosestSymbol createClosestSymbol(String name, long offset) {
       return new ClosestSymbol(name, offset);
    }
    private LoadObject createLoadObject(String fileName, long textsize,
                                        long base) {
       File f = new File(fileName);
       Address baseAddr = newAddress(base);
       return new SharedObject(this, fileName, f.length(), baseAddr);
    }
    private native static void init0()
                                throws DebuggerException;
    private native void attach0(int pid)
                                throws DebuggerException;
    private native void attach0(String execName, String coreName)
                                throws DebuggerException;
    private native void detach0()
                                throws DebuggerException;
    private native long lookupByName0(String objectName, String symbol)
                                throws DebuggerException;
    private native ClosestSymbol lookupByAddress0(long address)
                                throws DebuggerException;
    private native long[] getThreadIntegerRegisterSet0(int lwp_id)
                                throws DebuggerException;
    private native byte[] readBytesFromProcess0(long address, long numBytes)
                                throws DebuggerException;
    public native static int  getAddressSize() ;
    interface WorkerThreadTask {
       public void doit(LinuxDebuggerLocal debugger) throws DebuggerException;
    }
    class LinuxDebuggerLocalWorkerThread extends Thread {
       LinuxDebuggerLocal debugger;
       WorkerThreadTask task;
       DebuggerException lastException;
       public LinuxDebuggerLocalWorkerThread(LinuxDebuggerLocal debugger) {
         this.debugger = debugger;
         setDaemon(true);
       }
       public void run() {
          synchronized (workerThread) {
             for (;;) {
                if (task != null) {
                   lastException = null;
                   try {
                      task.doit(debugger);
                   } catch (DebuggerException exp) {
                      lastException = exp;
                   }
                   task = null;
                   workerThread.notifyAll();
                }
                try {
                   workerThread.wait();
                } catch (InterruptedException x) {}
             }
          }
       }
       public WorkerThreadTask execute(WorkerThreadTask task) throws DebuggerException {
          synchronized (workerThread) {
             this.task = task;
             workerThread.notifyAll();
             while (this.task != null) {
                try {
                   workerThread.wait();
                } catch (InterruptedException x) {}
             }
             if (lastException != null) {
                throw new DebuggerException(lastException);
             } else {
                return task;
             }
          }
       }
    }
    private LinuxDebuggerLocalWorkerThread workerThread = null;
    public LinuxDebuggerLocal(MachineDescription machDesc,
                              boolean useCache) throws DebuggerException {
        this.machDesc = machDesc;
        utils = new DebuggerUtilities(machDesc.getAddressSize(),
                                      machDesc.isBigEndian()) {
           public void checkAlignment(long address, long alignment) {
             if ( (address % alignment != 0)
                &&(alignment != 8 || address % 4 != 0)) {
                throw new UnalignedAddressException(
                        "Trying to read at address: "
                      + addressValueToString(address)
                      + " with alignment: " + alignment,
                        address);
             }
           }
        };
        if (useCache) {
            if (getCPU().equals("ia64")) {
              initCache(16384, parseCacheNumPagesProperty(1024));
            } else {
              initCache(4096, parseCacheNumPagesProperty(4096));
            }
        }
        workerThread = new LinuxDebuggerLocalWorkerThread(this);
        workerThread.start();
    }
    public boolean hasProcessList() throws DebuggerException {
        return false;
    }
    public List getProcessList() throws DebuggerException {
        throw new DebuggerException("getProcessList not implemented yet");
    }
    private void checkAttached() throws DebuggerException {
        if (attached) {
            if (isCore) {
                throw new DebuggerException("attached to a core dump already");
            } else {
                throw new DebuggerException("attached to a process already");
            }
        }
    }
    private void requireAttach() {
        if (! attached) {
            throw new RuntimeException("not attached to a process or a core!");
        }
    }
    private void findABIVersion() throws DebuggerException {
        if (lookupByName0("libjvm.so", "__vt_10JavaThread") != 0 ||
            lookupByName0("libjvm_g.so", "__vt_10JavaThread") != 0) {
            useGCC32ABI = false;
        } else {
            useGCC32ABI = true;
        }
    }
    public synchronized void attach(int processID) throws DebuggerException {
        checkAttached();
        threadList = new ArrayList();
        loadObjectList = new ArrayList();
        class AttachTask implements WorkerThreadTask {
           int pid;
           public void doit(LinuxDebuggerLocal debugger) {
              debugger.attach0(pid);
              debugger.attached = true;
              debugger.isCore = false;
              findABIVersion();
           }
        }
        AttachTask task = new AttachTask();
        task.pid = processID;
        workerThread.execute(task);
    }
    public synchronized void attach(String execName, String coreName) {
        checkAttached();
        threadList = new ArrayList();
        loadObjectList = new ArrayList();
        attach0(execName, coreName);
        attached = true;
        isCore = true;
        findABIVersion();
    }
    public synchronized boolean detach() {
        if (!attached) {
            return false;
        }
        threadList = null;
        loadObjectList = null;
        if (isCore) {
            detach0();
            attached = false;
            return true;
        } else {
            class DetachTask implements WorkerThreadTask {
                boolean result = false;
                public void doit(LinuxDebuggerLocal debugger) {
                    debugger.detach0();
                    debugger.attached = false;
                    result = true;
                }
            }
            DetachTask task = new DetachTask();
            workerThread.execute(task);
            return task.result;
        }
    }
    public Address parseAddress(String addressString)
            throws NumberFormatException {
        long addr = utils.scanAddress(addressString);
        if (addr == 0) {
            return null;
        }
        return new LinuxAddress(this, addr);
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
        throw new DebuggerException("No debugger console available on Linux");
    }
    public String getConsolePrompt() throws DebuggerException {
        return null;
    }
    private long handleGCC32ABI(long addr, String symbol) throws DebuggerException {
        if (useGCC32ABI && symbol.startsWith("_ZTV")) {
            return addr + (2 * machDesc.getAddressSize());
        } else {
            return addr;
        }
    }
    public synchronized Address lookup(String objectName, String symbol) {
        requireAttach();
        if (!attached) {
            return null;
        }
        if (isCore) {
            long addr = lookupByName0(objectName, symbol);
            return (addr == 0)? null : new LinuxAddress(this, handleGCC32ABI(addr, symbol));
        } else {
            class LookupByNameTask implements WorkerThreadTask {
                String objectName, symbol;
                Address result;
                public void doit(LinuxDebuggerLocal debugger) {
                    long addr = debugger.lookupByName0(objectName, symbol);
                    result = (addr == 0 ? null : new LinuxAddress(debugger, handleGCC32ABI(addr, symbol)));
                }
            }
            LookupByNameTask task = new LookupByNameTask();
            task.objectName = objectName;
            task.symbol = symbol;
            workerThread.execute(task);
            return task.result;
        }
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
        return new LinuxThread(this, addr);
    }
    public ThreadProxy getThreadForThreadId(long id) {
        return new LinuxThread(this, id);
    }
    public String addressValueToString(long address) {
        return utils.addressValueToString(address);
    }
    public LinuxAddress readAddress(long address)
            throws UnmappedAddressException, UnalignedAddressException {
        long value = readAddressValue(address);
        return (value == 0 ? null : new LinuxAddress(this, value));
    }
    public LinuxAddress readCompOopAddress(long address)
            throws UnmappedAddressException, UnalignedAddressException {
        long value = readCompOopAddressValue(address);
        return (value == 0 ? null : new LinuxAddress(this, value));
    }
    public LinuxOopHandle readOopHandle(long address)
            throws UnmappedAddressException, UnalignedAddressException,
                NotInHeapException {
        long value = readAddressValue(address);
        return (value == 0 ? null : new LinuxOopHandle(this, value));
    }
    public LinuxOopHandle readCompOopHandle(long address)
            throws UnmappedAddressException, UnalignedAddressException,
                NotInHeapException {
        long value = readCompOopAddressValue(address);
        return (value == 0 ? null : new LinuxOopHandle(this, value));
    }
    public synchronized long[] getThreadIntegerRegisterSet(int lwp_id)
                                            throws DebuggerException {
        requireAttach();
        if (isCore) {
            return getThreadIntegerRegisterSet0(lwp_id);
        } else {
            class GetThreadIntegerRegisterSetTask implements WorkerThreadTask {
                int lwp_id;
                long[] result;
                public void doit(LinuxDebuggerLocal debugger) {
                    result = debugger.getThreadIntegerRegisterSet0(lwp_id);
                }
            }
            GetThreadIntegerRegisterSetTask task = new GetThreadIntegerRegisterSetTask();
            task.lwp_id = lwp_id;
            workerThread.execute(task);
            return task.result;
        }
    }
    public long readCInteger(long address, long numBytes, boolean isUnsigned)
        throws UnmappedAddressException, UnalignedAddressException {
        if (numBytes == 8) {
            utils.checkAlignment(address, 4);
        } else {
            utils.checkAlignment(address, numBytes);
        }
        byte[] data = readBytes(address, numBytes);
        return utils.dataToCInteger(data, isUnsigned);
    }
    public long readJLong(long address)
        throws UnmappedAddressException, UnalignedAddressException {
        utils.checkAlignment(address, jintSize);
        byte[] data = readBytes(address, jlongSize);
        return utils.dataToJLong(data, jlongSize);
    }
    public long getAddressValue(Address addr) {
      if (addr == null) return 0;
      return ((LinuxAddress) addr).getValue();
    }
    public Address newAddress(long value) {
      if (value == 0) return null;
      return new LinuxAddress(this, value);
    }
    public List getThreadList() {
      requireAttach();
      return threadList;
    }
    public List getLoadObjectList() {
      requireAttach();
      return loadObjectList;
    }
    public synchronized ClosestSymbol lookup(long addr) {
       requireAttach();
       if (isCore) {
          return lookupByAddress0(addr);
       } else {
          class LookupByAddressTask implements WorkerThreadTask {
             long addr;
             ClosestSymbol result;
             public void doit(LinuxDebuggerLocal debugger) {
                 result = debugger.lookupByAddress0(addr);
             }
          }
          LookupByAddressTask task = new LookupByAddressTask();
          task.addr = addr;
          workerThread.execute(task);
          return task.result;
       }
    }
    public CDebugger getCDebugger() {
      if (cdbg == null) {
         String cpu = getCPU();
         if (cpu.equals("ia64") ) {
            return null;
         }
         cdbg = new LinuxCDebugger(this);
      }
      return cdbg;
    }
    public synchronized ReadResult readBytesFromProcess(long address,
            long numBytes) throws UnmappedAddressException, DebuggerException {
        requireAttach();
        if (isCore) {
            byte[] res = readBytesFromProcess0(address, numBytes);
            return (res != null)? new ReadResult(res) : new ReadResult(address);
        } else {
            class ReadBytesFromProcessTask implements WorkerThreadTask {
                long address, numBytes;
                ReadResult result;
                public void doit(LinuxDebuggerLocal debugger) {
                    byte[] res = debugger.readBytesFromProcess0(address, numBytes);
                    if (res != null)
                        result = new ReadResult(res);
                    else
                        result = new ReadResult(address);
                }
            }
            ReadBytesFromProcessTask task = new ReadBytesFromProcessTask();
            task.address = address;
            task.numBytes = numBytes;
            workerThread.execute(task);
            return task.result;
        }
    }
    public void writeBytesToProcess(long address, long numBytes, byte[] data)
        throws UnmappedAddressException, DebuggerException {
        throw new DebuggerException("Unimplemented");
    }
    static {
        System.loadLibrary("saproc");
        init0();
    }
}
