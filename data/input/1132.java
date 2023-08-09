public class ServiceabilityAgentJVMDIModule {
  private Debugger dbg;
  private String[] saLibNames;
  private String   saLibName;
  private boolean  attached;
  private boolean  suspended;
  private static final int JVMDI_EVENT_BREAKPOINT = 2;
  private static final int JVMDI_EVENT_EXCEPTION = 4;
  private static long timeoutMillis = 3000;
  private CIntegerAccessor saAttached;
  private CIntegerAccessor saEventPending;
  private CIntegerAccessor saEventKind;
  private JNIHandleAccessor saExceptionThread;
  private JNIHandleAccessor saExceptionClass;
  private JNIid             saExceptionMethod;
  private CIntegerAccessor  saExceptionLocation;
  private JNIHandleAccessor saExceptionException;
  private JNIHandleAccessor saExceptionCatchClass;
  private JNIid             saExceptionCatchMethod;
  private CIntegerAccessor  saExceptionCatchLocation;
  private JNIHandleAccessor saBreakpointThread;
  private JNIHandleAccessor saBreakpointClass;
  private JNIid             saBreakpointMethod;
  private CIntegerAccessor  saBreakpointLocation;
  private int               SA_CMD_SUSPEND_ALL;
  private int               SA_CMD_RESUME_ALL;
  private int               SA_CMD_TOGGLE_BREAKPOINT;
  private int               SA_CMD_BUF_SIZE;
  private CIntegerAccessor  saCmdPending;
  private CIntegerAccessor  saCmdType;
  private CIntegerAccessor  saCmdResult;
  private CStringAccessor   saCmdResultErrMsg;
  private CStringAccessor   saCmdBkptSrcFileName;
  private CStringAccessor   saCmdBkptPkgName;
  private CIntegerAccessor  saCmdBkptLineNumber;
  private CIntegerAccessor  saCmdBkptResWasError;
  private CIntegerAccessor  saCmdBkptResLineNumber;
  private CIntegerAccessor  saCmdBkptResBCI;
  private CIntegerAccessor  saCmdBkptResWasSet;
  private CStringAccessor   saCmdBkptResMethodName;
  private CStringAccessor   saCmdBkptResMethodSig;
  public ServiceabilityAgentJVMDIModule(Debugger dbg, String[] saLibNames) {
    this.dbg = dbg;
    this.saLibNames = saLibNames;
  }
  public boolean canAttach() {
    return setupLookup("SA_CMD_SUSPEND_ALL");
  }
  public void attach() throws DebuggerException {
    if (!canAttach()) {
      throw new DebuggerException("Unable to initiate symbol lookup in SA's JVMDI module");
    }
    if (attached) {
      throw new DebuggerException("Already attached");
    }
    SA_CMD_SUSPEND_ALL      = lookupConstInt("SA_CMD_SUSPEND_ALL");
    SA_CMD_RESUME_ALL       = lookupConstInt("SA_CMD_RESUME_ALL");
    SA_CMD_TOGGLE_BREAKPOINT = lookupConstInt("SA_CMD_TOGGLE_BREAKPOINT");
    SA_CMD_BUF_SIZE         = lookupConstInt("SA_CMD_BUF_SIZE");
    saAttached              = lookupCInt("saAttached");
    saEventPending          = lookupCInt("saEventPending");
    saEventKind             = lookupCInt("saEventKind");
    saCmdPending            = lookupCInt("saCmdPending");
    saCmdType               = lookupCInt("saCmdType");
    saCmdResult             = lookupCInt("saCmdResult");
    saCmdResultErrMsg       = lookupCString("saCmdResultErrMsg", SA_CMD_BUF_SIZE);
    saCmdBkptSrcFileName    = lookupCString("saCmdBkptSrcFileName", SA_CMD_BUF_SIZE);
    saCmdBkptPkgName        = lookupCString("saCmdBkptPkgName", SA_CMD_BUF_SIZE);
    saCmdBkptLineNumber     = lookupCInt("saCmdBkptLineNumber");
    saCmdBkptResWasError    = lookupCInt("saCmdBkptResWasError");
    saCmdBkptResLineNumber  = lookupCInt("saCmdBkptResLineNumber");
    saCmdBkptResBCI         = lookupCInt("saCmdBkptResBCI");
    saCmdBkptResWasSet      = lookupCInt("saCmdBkptResWasSet");
    saCmdBkptResMethodName  = lookupCString("saCmdBkptResMethodName", SA_CMD_BUF_SIZE);
    saCmdBkptResMethodSig   = lookupCString("saCmdBkptResMethodSig", SA_CMD_BUF_SIZE);
    lookup("saExceptionThread");
    lookup("saExceptionClass");
    lookup("saExceptionMethod");
    lookup("saExceptionLocation");
    lookup("saExceptionException");
    lookup("saExceptionCatchClass");
    lookup("saExceptionCatchMethod");
    lookup("saExceptionCatchLocation");
    lookup("saBreakpointThread");
    lookup("saBreakpointClass");
    lookup("saBreakpointMethod");
    lookup("saBreakpointLocation");
    saAttached.setValue(1);
    attached = true;
  }
  public void detach() {
    saAttached.setValue(0);
    attached = false;
    saLibName = null;
  }
  public void setCommandTimeout(long millis) {
    timeoutMillis = millis;
  }
  public long getCommandTimeout() {
    return timeoutMillis;
  }
  public boolean eventPending() {
    return (saEventPending.getValue() != 0);
  }
  public Event eventPoll() {
    if (saEventPending.getValue() == 0) {
      return null;
    }
    int kind = (int) saEventKind.getValue();
    switch (kind) {
    case JVMDI_EVENT_EXCEPTION: {
      JNIHandleAccessor thread = lookupJNIHandle("saExceptionThread");
      JNIHandleAccessor clazz = lookupJNIHandle("saExceptionClass");
      JNIid method = lookupJNIid("saExceptionMethod");
      CIntegerAccessor location = lookupCInt("saExceptionLocation");
      JNIHandleAccessor exception = lookupJNIHandle("saExceptionException");
      JNIHandleAccessor catchClass = lookupJNIHandle("saExceptionCatchClass");
      JNIid catchMethod = lookupJNIid("saExceptionCatchMethod");
      CIntegerAccessor catchLocation = lookupCInt("saExceptionCatchLocation");
      return new ExceptionEvent(thread.getValue(), clazz.getValue(), method,
                                (int) location.getValue(), exception.getValue(),
                                catchClass.getValue(), catchMethod, (int) catchLocation.getValue());
    }
    case JVMDI_EVENT_BREAKPOINT: {
      JNIHandleAccessor thread = lookupJNIHandle("saBreakpointThread");
      JNIHandleAccessor clazz = lookupJNIHandle("saBreakpointClass");
      JNIid method = lookupJNIid("saBreakpointMethod");
      CIntegerAccessor location = lookupCInt("saBreakpointLocation");
      return new BreakpointEvent(thread.getValue(), clazz.getValue(),
                                 method, (int) location.getValue());
    }
    default:
      throw new DebuggerException("Unsupported event type " + kind);
    }
  }
  public void eventContinue() {
    saEventPending.setValue(0);
  }
  public void suspend() {
    saCmdType.setValue(SA_CMD_SUSPEND_ALL);
    saCmdPending.setValue(1);
    waitForCommandCompletion();
    suspended = true;
  }
  public void resume() {
    saCmdType.setValue(SA_CMD_RESUME_ALL);
    saCmdPending.setValue(1);
    waitForCommandCompletion();
    suspended = false;
  }
  public boolean isSuspended() {
    return suspended;
  }
  public static class BreakpointToggleResult {
    private boolean success;
    private String errMsg;
    private int lineNumber;
    private int bci;
    private boolean wasSet;
    private String methodName;
    private String methodSig;
    public BreakpointToggleResult(int lineNumber, int bci, boolean wasSet,
                                  String methodName, String methodSig) {
      this.lineNumber = lineNumber;
      this.bci = bci;
      this.wasSet = wasSet;
      this.methodName = methodName;
      this.methodSig = methodSig;
      success = true;
    }
    public BreakpointToggleResult(String errMsg) {
      this.errMsg = errMsg;
      success = false;
    }
    public boolean getSuccess() { return success; }
    public String getErrMsg() { return errMsg; }
    public int getLineNumber() { return lineNumber; }
    public int getBCI() { return bci; }
    public boolean getWasSet() { return wasSet; }
    public String getMethodName() { return methodName; }
    public String getMethodSignature() { return methodSig; }
  }
  public BreakpointToggleResult toggleBreakpoint(String srcFileName,
                                                 String pkgName,
                                                 int lineNo) {
    saCmdBkptSrcFileName.setValue(srcFileName);
    saCmdBkptPkgName.setValue(pkgName);
    saCmdBkptLineNumber.setValue(lineNo);
    saCmdType.setValue(SA_CMD_TOGGLE_BREAKPOINT);
    saCmdPending.setValue(1);
    if (waitForCommandCompletion(true)) {
      return new BreakpointToggleResult((int) saCmdBkptResLineNumber.getValue(),
                                        (int) saCmdBkptResBCI.getValue(),
                                        (saCmdBkptResWasSet.getValue() != 0),
                                        saCmdBkptResMethodName.getValue(),
                                        saCmdBkptResMethodSig.getValue());
    } else {
      return new BreakpointToggleResult(saCmdResultErrMsg.getValue());
    }
  }
  private CIntegerAccessor lookupCInt(String symbolName) {
    return new CIntegerAccessor(lookup(symbolName), 4, false);
  }
  private CStringAccessor lookupCString(String symbolName, int bufLen) {
    return new CStringAccessor(lookup(symbolName), bufLen);
  }
  private JNIHandleAccessor lookupJNIHandle(String symbolName) {
    return new JNIHandleAccessor(lookup(symbolName), VM.getVM().getObjectHeap());
  }
  private JNIid lookupJNIid(String symbolName) {
    Address idAddr = lookup(symbolName).getAddressAt(0);
    if (idAddr == null) {
      return null;
    }
    return new JNIid(idAddr, VM.getVM().getObjectHeap());
  }
  private int lookupConstInt(String symbolName) {
    Address addr = lookup(symbolName);
    return (int) addr.getCIntegerAt(0, 4, false);
  }
  private boolean setupLookup(String symbolName) {
    if (saLibName == null) {
      for (int i = 0; i < saLibNames.length; i++) {
        Address addr = dbg.lookup(saLibNames[i], symbolName);
        if (addr != null) {
          saLibName = saLibNames[i];
          return true;
        }
      }
      return false;
    }
    return true;
  }
  private Address lookup(String symbolName) {
    if (saLibName == null) {
      for (int i = 0; i < saLibNames.length; i++) {
        Address addr = dbg.lookup(saLibNames[i], symbolName);
        if (addr != null) {
          saLibName = saLibNames[i];
          return addr;
        }
      }
      throw new DebuggerException("Unable to find symbol " + symbolName + " in any of the known names for the SA");
    }
    Address addr = dbg.lookup(saLibName, symbolName);
    if (addr == null) {
      throw new DebuggerException("Unable to find symbol " + symbolName + " in " + saLibName);
    }
    return addr;
  }
  private void waitForCommandCompletion() {
    waitForCommandCompletion(false);
  }
  private boolean waitForCommandCompletion(boolean forBreakpoint) {
    long start = System.currentTimeMillis();
    long cur = start;
    while ((saCmdPending.getValue() != 0) &&
           (cur - start < timeoutMillis)) {
      try {
        java.lang.Thread.currentThread().sleep(10);
      } catch (InterruptedException e) {
      }
      cur = System.currentTimeMillis();
    }
    if (saCmdPending.getValue() != 0) {
      detach();
      throw new DebuggerException("VM appears to have died");
    }
    boolean succeeded = saCmdResult.getValue() == 0;
    if (!succeeded &&
        (!forBreakpoint || saCmdBkptResWasError.getValue() != 0)) {
      String err = saCmdResultErrMsg.getValue();
      throw new DebuggerException("Error executing JVMDI command: " + err);
    }
    return succeeded;
  }
}
