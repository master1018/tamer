public class Win32AMD64JavaThreadPDAccess implements JavaThreadPDAccess {
  private static AddressField  lastJavaFPField;
  private static AddressField  osThreadField;
  private static Field         osThreadThreadHandleField;
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
    Type anchorType = db.lookupType("JavaFrameAnchor");
    lastJavaFPField         = anchorType.getAddressField("_last_Java_fp");
    osThreadField           = type.getAddressField("_osthread");
    type = db.lookupType("OSThread");
    osThreadThreadHandleField = type.getField("_thread_handle");
  }
  public Address getLastJavaFP(Address addr) {
    return lastJavaFPField.getValue(addr.addOffsetTo(sun.jvm.hotspot.runtime.JavaThread.getAnchorField().getOffset()));
  }
  public Address getLastJavaPC(Address addr) {
    return null;
  }
  public Address getBaseOfStackPointer(Address addr) {
    return null;
  }
  public Frame getLastFramePD(JavaThread thread, Address addr) {
    Address fp = thread.getLastJavaFP();
    if (fp == null) {
      return null; 
    }
    Address pc =  thread.getLastJavaPC();
    if ( pc != null ) {
      return new AMD64Frame(thread.getLastJavaSP(), fp, pc);
    } else {
      return new AMD64Frame(thread.getLastJavaSP(), fp);
    }
  }
  public RegisterMap newRegisterMap(JavaThread thread, boolean updateMap) {
    return new AMD64RegisterMap(thread, updateMap);
  }
  public Frame getCurrentFrameGuess(JavaThread thread, Address addr) {
    ThreadProxy t = getThreadProxy(addr);
    AMD64ThreadContext context = (AMD64ThreadContext) t.getContext();
    AMD64CurrentFrameGuess guesser = new AMD64CurrentFrameGuess(context, thread);
    if (!guesser.run(GUESS_SCAN_RANGE)) {
      return null;
    }
    if (guesser.getPC() == null) {
      return new AMD64Frame(guesser.getSP(), guesser.getFP());
    } else {
      return new AMD64Frame(guesser.getSP(), guesser.getFP(), guesser.getPC());
    }
  }
  public void printThreadIDOn(Address addr, PrintStream tty) {
    tty.print(getThreadProxy(addr));
  }
  public void printInfoOn(Address threadAddr, PrintStream tty) {
  }
  public Address getLastSP(Address addr) {
    ThreadProxy t = getThreadProxy(addr);
    AMD64ThreadContext context = (AMD64ThreadContext) t.getContext();
    return context.getRegisterAsAddress(AMD64ThreadContext.RSP);
  }
  public ThreadProxy getThreadProxy(Address addr) {
    Address osThreadAddr = osThreadField.getValue(addr);
    Address threadHandleAddr =
      osThreadAddr.addOffsetTo(osThreadThreadHandleField.getOffset());
    JVMDebugger debugger = VM.getVM().getDebugger();
    return debugger.getThreadForIdentifierAddress(threadHandleAddr);
  }
}
