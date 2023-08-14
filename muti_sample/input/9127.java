public class X86Frame extends Frame {
  private static final boolean DEBUG;
  static {
    DEBUG = System.getProperty("sun.jvm.hotspot.runtime.x86.X86Frame.DEBUG") != null;
  }
  private static final int LINK_OFFSET                =  0;
  private static final int RETURN_ADDR_OFFSET         =  1;
  private static final int SENDER_SP_OFFSET           =  2;
  private static final int INTERPRETER_FRAME_MIRROR_OFFSET    =  2; 
  private static final int INTERPRETER_FRAME_SENDER_SP_OFFSET = -1;
  private static final int INTERPRETER_FRAME_LAST_SP_OFFSET   = INTERPRETER_FRAME_SENDER_SP_OFFSET - 1;
  private static final int INTERPRETER_FRAME_METHOD_OFFSET    = INTERPRETER_FRAME_LAST_SP_OFFSET - 1;
  private static       int INTERPRETER_FRAME_MDX_OFFSET;         
  private static       int INTERPRETER_FRAME_CACHE_OFFSET;
  private static       int INTERPRETER_FRAME_LOCALS_OFFSET;
  private static       int INTERPRETER_FRAME_BCX_OFFSET;
  private static       int INTERPRETER_FRAME_INITIAL_SP_OFFSET;
  private static       int INTERPRETER_FRAME_MONITOR_BLOCK_TOP_OFFSET;
  private static       int INTERPRETER_FRAME_MONITOR_BLOCK_BOTTOM_OFFSET;
  private static final int ENTRY_FRAME_CALL_WRAPPER_OFFSET   =  2;
  private static final int NATIVE_FRAME_INITIAL_PARAM_OFFSET =  2;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    if (VM.getVM().isCore()) {
      INTERPRETER_FRAME_CACHE_OFFSET = INTERPRETER_FRAME_METHOD_OFFSET - 1;
    } else {
      INTERPRETER_FRAME_MDX_OFFSET   = INTERPRETER_FRAME_METHOD_OFFSET - 1;
      INTERPRETER_FRAME_CACHE_OFFSET = INTERPRETER_FRAME_MDX_OFFSET - 1;
    }
    INTERPRETER_FRAME_LOCALS_OFFSET               = INTERPRETER_FRAME_CACHE_OFFSET - 1;
    INTERPRETER_FRAME_BCX_OFFSET                  = INTERPRETER_FRAME_LOCALS_OFFSET - 1;
    INTERPRETER_FRAME_INITIAL_SP_OFFSET           = INTERPRETER_FRAME_BCX_OFFSET - 1;
    INTERPRETER_FRAME_MONITOR_BLOCK_TOP_OFFSET    = INTERPRETER_FRAME_INITIAL_SP_OFFSET;
    INTERPRETER_FRAME_MONITOR_BLOCK_BOTTOM_OFFSET = INTERPRETER_FRAME_INITIAL_SP_OFFSET;
  }
  Address raw_fp; 
  private Address raw_unextendedSP;
  private X86Frame() {
  }
  private void adjustForDeopt() {
    if ( pc != null) {
      CodeBlob cb = VM.getVM().getCodeCache().findBlob(pc);
      if (cb != null && cb.isJavaMethod()) {
        NMethod nm = (NMethod) cb;
        if (pc.equals(nm.deoptBegin())) {
          if (Assert.ASSERTS_ENABLED) {
            Assert.that(this.getUnextendedSP() != null, "null SP in Java frame");
          }
          pc = this.getUnextendedSP().getAddressAt(nm.origPCOffset());
          deoptimized = true;
        }
      }
    }
  }
  public X86Frame(Address raw_sp, Address raw_fp, Address pc) {
    this.raw_sp = raw_sp;
    this.raw_unextendedSP = raw_sp;
    this.raw_fp = raw_fp;
    this.pc = pc;
    adjustForDeopt();
    if (DEBUG) {
      System.out.println("X86Frame(sp, fp, pc): " + this);
      dumpStack();
    }
  }
  public X86Frame(Address raw_sp, Address raw_fp) {
    this.raw_sp = raw_sp;
    this.raw_unextendedSP = raw_sp;
    this.raw_fp = raw_fp;
    this.pc = raw_sp.getAddressAt(-1 * VM.getVM().getAddressSize());
    adjustForDeopt();
    if (DEBUG) {
      System.out.println("X86Frame(sp, fp): " + this);
      dumpStack();
    }
  }
  public X86Frame(Address raw_sp, Address raw_fp, long extension) {
    this.raw_sp = raw_sp;
    if (raw_sp == null) {
      this.raw_unextendedSP = null;
    } else {
      this.raw_unextendedSP = raw_sp.addOffsetTo(extension);
    }
    this.raw_fp = raw_fp;
    this.pc = raw_sp.getAddressAt(-1 * VM.getVM().getAddressSize());
    adjustForDeopt();
    if (DEBUG) {
      System.out.println("X86Frame(sp, fp): " + this);
      dumpStack();
    }
  }
  public Object clone() {
    X86Frame frame = new X86Frame();
    frame.raw_sp = raw_sp;
    frame.raw_unextendedSP = raw_unextendedSP;
    frame.raw_fp = raw_fp;
    frame.raw_fp = raw_fp;
    frame.pc = pc;
    frame.deoptimized = deoptimized;
    return frame;
  }
  public boolean equals(Object arg) {
    if (arg == null) {
      return false;
    }
    if (!(arg instanceof X86Frame)) {
      return false;
    }
    X86Frame other = (X86Frame) arg;
    return (AddressOps.equal(getSP(), other.getSP()) &&
            AddressOps.equal(getUnextendedSP(), other.getUnextendedSP()) &&
            AddressOps.equal(getFP(), other.getFP()) &&
            AddressOps.equal(getPC(), other.getPC()));
  }
  public int hashCode() {
    if (raw_sp == null) {
      return 0;
    }
    return raw_sp.hashCode();
  }
  public String toString() {
    return "sp: " + (getSP() == null? "null" : getSP().toString()) +
         ", unextendedSP: " + (getUnextendedSP() == null? "null" : getUnextendedSP().toString()) +
         ", fp: " + (getFP() == null? "null" : getFP().toString()) +
         ", pc: " + (pc == null? "null" : pc.toString());
  }
  public Address getFP() { return raw_fp; }
  public Address getSP() { return raw_sp; }
  public Address getID() { return raw_sp; }
  public boolean isSignalHandlerFrameDbg() { return false; }
  public int     getSignalNumberDbg()      { return 0;     }
  public String  getSignalNameDbg()        { return null;  }
  public boolean isInterpretedFrameValid() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isInterpretedFrame(), "Not an interpreted frame");
    }
    if (getFP() == null || getFP().andWithMask(0x3) != null) {
      return false;
    }
    if (getSP() == null || getSP().andWithMask(0x3) != null) {
      return false;
    }
    if (getFP().addOffsetTo(INTERPRETER_FRAME_INITIAL_SP_OFFSET * VM.getVM().getAddressSize()).lessThan(getSP())) {
      return false;
    }
    if (getFP().lessThanOrEqual(getSP())) {
      return false;
    }
    if (getFP().minus(getSP()) > 4096 * VM.getVM().getAddressSize()) {
      return false;
    }
    return true;
  }
  public Frame sender(RegisterMap regMap, CodeBlob cb) {
    X86RegisterMap map = (X86RegisterMap) regMap;
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map != null, "map must be set");
    }
    map.setIncludeArgumentOops(false);
    if (isEntryFrame())       return senderForEntryFrame(map);
    if (isInterpretedFrame()) return senderForInterpreterFrame(map);
    if (!VM.getVM().isCore()) {
      if(cb == null) {
        cb = VM.getVM().getCodeCache().findBlob(getPC());
      } else {
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(cb.equals(VM.getVM().getCodeCache().findBlob(getPC())), "Must be the same");
        }
      }
      if (cb != null) {
         return senderForCompiledFrame(map, cb);
      }
    }
    return new X86Frame(getSenderSP(), getLink(), getSenderPC());
  }
  private Frame senderForEntryFrame(X86RegisterMap map) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map != null, "map must be set");
    }
    X86JavaCallWrapper jcw = (X86JavaCallWrapper) getEntryFrameCallWrapper();
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(!entryFrameIsFirst(), "next Java fp must be non zero");
      Assert.that(jcw.getLastJavaSP().greaterThan(getSP()), "must be above this frame on stack");
    }
    X86Frame fr;
    if (jcw.getLastJavaPC() != null) {
      fr = new X86Frame(jcw.getLastJavaSP(), jcw.getLastJavaFP(), jcw.getLastJavaPC());
    } else {
      fr = new X86Frame(jcw.getLastJavaSP(), jcw.getLastJavaFP());
    }
    map.clear();
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map.getIncludeArgumentOops(), "should be set by clear");
    }
    return fr;
  }
  private Frame senderForInterpreterFrame(X86RegisterMap map) {
    Address unextendedSP = addressOfStackSlot(INTERPRETER_FRAME_SENDER_SP_OFFSET).getAddressAt(0);
    Address sp = addressOfStackSlot(SENDER_SP_OFFSET);
    return new X86Frame(sp, getLink(), unextendedSP.minus(sp));
  }
  private Frame senderForCompiledFrame(X86RegisterMap map, CodeBlob cb) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map != null, "map must be set");
    }
    Address        sender_sp = null;
    if (VM.getVM().isClientCompiler()) {
      sender_sp        = addressOfStackSlot(SENDER_SP_OFFSET);
    } else {
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(cb.getFrameSize() >= 0, "Compiled by Compiler1: do not use");
      }
      sender_sp = getUnextendedSP().addOffsetTo(cb.getFrameSize());
    }
    Address sender_pc = sender_sp.getAddressAt(-1 * VM.getVM().getAddressSize());
    if (map.getUpdateMap() && cb.getOopMaps() != null) {
      OopMapSet.updateRegisterMap(this, cb, map, true);
    }
    if (VM.getVM().isClientCompiler()) {
      map.setIncludeArgumentOops(cb.callerMustGCArguments(map.getThread()));
    }
    Address saved_fp = null;
    if (VM.getVM().isClientCompiler()) {
      saved_fp = getFP().getAddressAt(0);
    } else if (VM.getVM().isServerCompiler() &&
               (VM.getVM().getInterpreter().contains(sender_pc) ||
               VM.getVM().getStubRoutines().returnsToCallStub(sender_pc))) {
      saved_fp = sender_sp.getAddressAt(-2 * VM.getVM().getAddressSize());
    }
    return new X86Frame(sender_sp, saved_fp, sender_pc);
  }
  protected boolean hasSenderPD() {
    return true;
  }
  public long frameSize() {
    return (getSenderSP().minus(getSP()) / VM.getVM().getAddressSize());
  }
  public Address getLink() {
    return addressOfStackSlot(LINK_OFFSET).getAddressAt(0);
  }
  public Address getUnextendedSP() { return raw_unextendedSP; }
  public Address getSenderPCAddr() { return addressOfStackSlot(RETURN_ADDR_OFFSET); }
  public Address getSenderPC()     { return getSenderPCAddr().getAddressAt(0);      }
  public Address getNativeParamAddr(int idx) {
    return addressOfStackSlot(NATIVE_FRAME_INITIAL_PARAM_OFFSET + idx);
  }
  public Address getSenderSP()     { return addressOfStackSlot(SENDER_SP_OFFSET); }
  public Address compiledArgumentToLocationPD(VMReg reg, RegisterMap regMap, int argSize) {
    if (VM.getVM().isCore() || VM.getVM().isClientCompiler()) {
      throw new RuntimeException("Should not reach here");
    }
    return oopMapRegToLocation(reg, regMap);
  }
  public Address addressOfInterpreterFrameLocals() {
    return addressOfStackSlot(INTERPRETER_FRAME_LOCALS_OFFSET);
  }
  private Address addressOfInterpreterFrameBCX() {
    return addressOfStackSlot(INTERPRETER_FRAME_BCX_OFFSET);
  }
  public int getInterpreterFrameBCI() {
    Address bcp = addressOfInterpreterFrameBCX().getAddressAt(0);
    OopHandle methodHandle = addressOfInterpreterFrameMethod().getOopHandleAt(0);
    Method method = (Method) VM.getVM().getObjectHeap().newOop(methodHandle);
    return bcpToBci(bcp, method);
  }
  public Address addressOfInterpreterFrameMDX() {
    return addressOfStackSlot(INTERPRETER_FRAME_MDX_OFFSET);
  }
  public Address addressOfInterpreterFrameExpressionStack() {
    Address monitorEnd = interpreterFrameMonitorEnd().address();
    return monitorEnd.addOffsetTo(-1 * VM.getVM().getAddressSize());
  }
  public int getInterpreterFrameExpressionStackDirection() { return -1; }
  public Address addressOfInterpreterFrameTOS() {
    return getSP();
  }
  public Address addressOfInterpreterFrameTOSAt(int slot) {
    return addressOfInterpreterFrameTOS().addOffsetTo(slot * VM.getVM().getAddressSize());
  }
  public Address getInterpreterFrameSenderSP() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isInterpretedFrame(), "interpreted frame expected");
    }
    return addressOfStackSlot(INTERPRETER_FRAME_SENDER_SP_OFFSET).getAddressAt(0);
  }
  public BasicObjectLock interpreterFrameMonitorBegin() {
    return new BasicObjectLock(addressOfStackSlot(INTERPRETER_FRAME_MONITOR_BLOCK_BOTTOM_OFFSET));
  }
  public BasicObjectLock interpreterFrameMonitorEnd() {
    Address result = addressOfStackSlot(INTERPRETER_FRAME_MONITOR_BLOCK_TOP_OFFSET).getAddressAt(0);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(AddressOps.gt(getFP(), result), "result must <  than frame pointer");
      Assert.that(AddressOps.lte(getSP(), result), "result must >= than stack pointer");
    }
    return new BasicObjectLock(result);
  }
  public int interpreterFrameMonitorSize() {
    return BasicObjectLock.size();
  }
  public Address addressOfInterpreterFrameMethod() {
    return addressOfStackSlot(INTERPRETER_FRAME_METHOD_OFFSET);
  }
  public Address addressOfInterpreterFrameCPCache() {
    return addressOfStackSlot(INTERPRETER_FRAME_CACHE_OFFSET);
  }
  public JavaCallWrapper getEntryFrameCallWrapper() {
    return new X86JavaCallWrapper(addressOfStackSlot(ENTRY_FRAME_CALL_WRAPPER_OFFSET).getAddressAt(0));
  }
  protected Address addressOfSavedOopResult() {
    return getSP().addOffsetTo((VM.getVM().isClientCompiler() ? 2 : 3) *
                               VM.getVM().getAddressSize());
  }
  protected Address addressOfSavedReceiver() {
    return getSP().addOffsetTo(-4 * VM.getVM().getAddressSize());
  }
  private void dumpStack() {
    if (getFP() != null) {
      for (Address addr = getSP().addOffsetTo(-5 * VM.getVM().getAddressSize());
           AddressOps.lte(addr, getFP().addOffsetTo(5 * VM.getVM().getAddressSize()));
           addr = addr.addOffsetTo(VM.getVM().getAddressSize())) {
        System.out.println(addr + ": " + addr.getAddressAt(0));
      }
    } else {
      for (Address addr = getSP().addOffsetTo(-5 * VM.getVM().getAddressSize());
           AddressOps.lte(addr, getSP().addOffsetTo(20 * VM.getVM().getAddressSize()));
           addr = addr.addOffsetTo(VM.getVM().getAddressSize())) {
        System.out.println(addr + ": " + addr.getAddressAt(0));
      }
    }
  }
}
