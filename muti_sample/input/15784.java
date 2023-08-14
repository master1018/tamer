public class Win32IA64JavaThreadPDAccess implements JavaThreadPDAccess {
  private static AddressField  lastJavaIFrameField;
  private static AddressField  osThreadField;
  private static CIntegerField osThreadPThreadIDField;
  private static final long GUESS_SCAN_RANGE = 128 * 1024;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("JavaThread");
    lastJavaIFrameField     = type.getAddressField("_last_Java_iframe");
    osThreadField           = type.getAddressField("_osthread");
    type = db.lookupType("OSThread");
    osThreadPThreadIDField   = type.getCIntegerField("_pthread_id");
  }
  public    Address getLastJavaIFrame(Address addr) {
    return lastJavaIFrameField.getValue(addr);
  }
  public    Address getBaseOfStackPointer(Address addr) {
    return null;
  }
  public Address getLastJavaFP(Address addr) {
    return null; 
  }
  public    Address getLastJavaPC(Address addr) {
    return null; 
  }
  public boolean isInterpretedFrame() {
    return true;
  }
  public    Frame getLastFramePD(JavaThread thread, Address addr) {
    Address iframe = getLastJavaIFrame(addr);
    Address pc = thread.getLastJavaPC();
    if (iframe == null) {
      return null; 
    }
    return new IA64Frame(thread.getLastJavaSP(), iframe, pc);
  }
  public    RegisterMap newRegisterMap(JavaThread thread, boolean updateMap) {
    return new IA64RegisterMap(thread, updateMap);
  }
  public    Frame getCurrentFrameGuess(JavaThread thread, Address addr) {
    return getLastFramePD(thread, addr);
  }
  public    void printThreadIDOn(Address addr, PrintStream tty) {
    tty.print(getThreadProxy(addr));
  }
  public    void printInfoOn(Address threadAddr, PrintStream tty) {
    tty.print("Thread id: ");
    printThreadIDOn(threadAddr, tty);
    tty.println("\nLastJavaIFrame: " + getLastJavaIFrame(threadAddr));
  }
  public    Address getLastSP(Address addr) {
    ThreadProxy t = getThreadProxy(addr);
    IA64ThreadContext context = (IA64ThreadContext) t.getContext();
    return context.getRegisterAsAddress(IA64ThreadContext.SP);
  }
  public    ThreadProxy getThreadProxy(Address addr) {
    Address osThreadAddr = osThreadField.getValue(addr);
    Address pthreadIdAddr = osThreadAddr.addOffsetTo(osThreadPThreadIDField.getOffset());
    JVMDebugger debugger = VM.getVM().getDebugger();
    return debugger.getThreadForIdentifierAddress(pthreadIdAddr);
  }
}
