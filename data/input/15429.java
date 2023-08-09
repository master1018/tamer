public class X86CurrentFrameGuess {
  private X86ThreadContext context;
  private JavaThread       thread;
  private Address          spFound;
  private Address          fpFound;
  private Address          pcFound;
  private static final boolean DEBUG = System.getProperty("sun.jvm.hotspot.runtime.x86.X86Frame.DEBUG")
                                       != null;
  public X86CurrentFrameGuess(X86ThreadContext context,
                              JavaThread thread) {
    this.context = context;
    this.thread  = thread;
  }
  public boolean run(long regionInBytesToSearch) {
    Address sp  = context.getRegisterAsAddress(X86ThreadContext.SP);
    Address pc  = context.getRegisterAsAddress(X86ThreadContext.PC);
    Address fp  = context.getRegisterAsAddress(X86ThreadContext.FP);
    if (sp == null) {
      if (thread.getLastJavaSP() != null) {
        setValues(thread.getLastJavaSP(), thread.getLastJavaFP(), null);
        return true;
      }
      return false;
    }
    Address end = sp.addOffsetTo(regionInBytesToSearch);
    VM vm       = VM.getVM();
    setValues(null, null, null); 
    if (vm.isJavaPCDbg(pc)) {
      if (vm.isClientCompiler()) {
        setValues(sp, fp, pc);
        return true;
      } else {
        if (vm.getInterpreter().contains(pc)) {
          if (DEBUG) {
            System.out.println("CurrentFrameGuess: choosing interpreter frame: sp = " +
                               sp + ", fp = " + fp + ", pc = " + pc);
          }
          setValues(sp, fp, pc);
          return true;
        }
        for (long offset = 0;
             offset < regionInBytesToSearch;
             offset += vm.getAddressSize()) {
          try {
            Address curSP = sp.addOffsetTo(offset);
            Frame frame = new X86Frame(curSP, null, pc);
            RegisterMap map = thread.newRegisterMap(false);
            while (frame != null) {
              if (frame.isEntryFrame() && frame.entryFrameIsFirst()) {
                if (DEBUG) {
                  System.out.println("CurrentFrameGuess: Choosing sp = " + curSP + ", pc = " + pc);
                }
                setValues(curSP, null, pc);
                return true;
              }
              frame = frame.sender(map);
            }
          } catch (Exception e) {
            if (DEBUG) {
              System.out.println("CurrentFrameGuess: Exception " + e + " at offset " + offset);
            }
          }
        }
        return false;
      }
    } else {
      if (DEBUG) {
        System.out.println("CurrentFrameGuess: choosing last Java frame: sp = " +
                           thread.getLastJavaSP() + ", fp = " + thread.getLastJavaFP());
      }
      if (thread.getLastJavaSP() == null) {
        return false; 
      }
      setValues(thread.getLastJavaSP(), thread.getLastJavaFP(), null);
      return true;
    }
  }
  public Address getSP() { return spFound; }
  public Address getFP() { return fpFound; }
  public Address getPC() { return pcFound; }
  private void setValues(Address sp, Address fp, Address pc) {
    spFound = sp;
    fpFound = fp;
    pcFound = pc;
  }
}
