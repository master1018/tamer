public class SPARCFrame extends Frame {
  private Address raw_youngerSP;
  private long    interpreterSPAdjustmentOffset;
  private static final int WORDS_PER_LONG = 2;
  public static final int PC_RETURN_OFFSET = 8;
  public static final int REGISTER_SAVE_WORDS   = 16;
  public static final int CALLEE_AGGREGATE_RETURN_POINTER_WORDS     = 1;
  public static final int CALLEE_REGISTER_ARGUMENT_SAVE_AREA_WORDS  = 6;
  public static final int REGISTER_SAVE_WORDS_SP_OFFSET             = 0;
  public static final int CALLEE_AGGREGATE_RETURN_POINTER_SP_OFFSET = REGISTER_SAVE_WORDS_SP_OFFSET + REGISTER_SAVE_WORDS;
  public static final int CALLEE_REGISTER_ARGUMENT_SAVE_AREA_SP_OFFSET = (CALLEE_AGGREGATE_RETURN_POINTER_SP_OFFSET +
                                                                          CALLEE_AGGREGATE_RETURN_POINTER_WORDS);
  public static final int MEMORY_PARAMETER_WORD_SP_OFFSET              = (CALLEE_REGISTER_ARGUMENT_SAVE_AREA_SP_OFFSET +
                                                                          CALLEE_REGISTER_ARGUMENT_SAVE_AREA_WORDS);
  public static final int VARARGS_OFFSET                               = MEMORY_PARAMETER_WORD_SP_OFFSET;
  private static final boolean DEBUG = System.getProperty("sun.jvm.hotspot.runtime.sparc.SPARCFrame.DEBUG") != null;
  public static Address unBiasSP(Address raw_sp) {
    if (raw_sp != null) {
      return raw_sp.addOffsetTo(VM.getVM().getStackBias());
    } else {
      return null;
    }
  }
  public static Address biasSP(Address real_sp) {
    if (real_sp != null) {
      if (DEBUG) {
        System.out.println("biasing realsp: " + real_sp + " biased: " + real_sp.addOffsetTo(-VM.getVM().getStackBias()) );
      }
      return real_sp.addOffsetTo(-VM.getVM().getStackBias());
    } else {
      if (DEBUG) {
        System.out.println("biasing null realsp");
      }
      return null;
    }
  }
  public static Address findYoungerSP(Address top, Address find) {
    Address findRaw = biasSP(find);
    if (top == null || find == null || findRaw == null) {
      throw new RuntimeException("bad values for findYoungerSP top: " + top + " find: " + find);
    }
    final int maxFrames = 20;
    int count = 0;
    Address search = top;
    Address next;
    Address pc;
    if (DEBUG) {
      System.out.println("findYoungerSP top: " + top + " find: " + find + " findRaw: " + findRaw);
    }
    while ( count != maxFrames && search != null) {
      next = search.getAddressAt(SPARCRegisters.I6.spOffsetInSavedWindow());
      pc = search.getAddressAt(SPARCRegisters.I7.spOffsetInSavedWindow());
      if (DEBUG) {
        System.out.println("findYoungerSP next: " + next + " pc: " + pc);
      }
      if (next.equals(findRaw)) {
        return search;
      }
      search = unBiasSP(next);
    }
    if (DEBUG) {
      System.out.println("findYoungerSP: never found younger, top: " + top + " find: " + find);
    }
    return null;
  }
  public Address getSP()              {
    if (DEBUG) {
      System.out.println("getSP raw: " + raw_sp + " unbiased: " + unBiasSP(raw_sp));
    }
    return  unBiasSP(raw_sp);
  }
  public Address getID()              {
    return getSP();
  }
  public Address getYoungerSP()       {
    if (DEBUG) {
      System.out.println("getYoungerSP: " + raw_youngerSP + " unbiased: " + unBiasSP(raw_youngerSP));
    }
    return unBiasSP(raw_youngerSP);
  }
  public SPARCFrame(Address raw_sp, Address raw_youngerSP, boolean youngerFrameIsInterpreted) {
    super();
    if (DEBUG) {
      System.out.println("Constructing frame(1) raw_sp: " + raw_sp + " raw_youngerSP: " + raw_youngerSP);
    }
    if (Assert.ASSERTS_ENABLED) {
      Assert.that((unBiasSP(raw_sp).andWithMask(VM.getVM().getAddressSize() - 1) == null),
                   "Expected raw sp likely got real sp, value was " + raw_sp);
      if (raw_youngerSP != null) {
        Assert.that((unBiasSP(raw_youngerSP).andWithMask(VM.getVM().getAddressSize() - 1) == null),
                    "Expected raw youngerSP likely got real youngerSP, value was " + raw_youngerSP);
      }
    }
    this.raw_sp = raw_sp;
    this.raw_youngerSP = raw_youngerSP;
    if (raw_youngerSP == null) {
      pc = null;
    } else {
      Address youngerSP = unBiasSP(raw_youngerSP);
      pc = youngerSP.getAddressAt(SPARCRegisters.I7.spOffsetInSavedWindow()).addOffsetTo(PC_RETURN_OFFSET);
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(youngerSP.getAddressAt(SPARCRegisters.FP.spOffsetInSavedWindow()).
                    equals(raw_sp),
                    "youngerSP must be valid");
      }
    }
    if (youngerFrameIsInterpreted) {
      long IsavedSP = SPARCRegisters.IsavedSP.spOffsetInSavedWindow();
      interpreterSPAdjustmentOffset = 0;
      Address savedSP = unBiasSP(getYoungerSP().getAddressAt(IsavedSP));
      if (savedSP == null) {
        if ( DEBUG) {
          System.out.println("WARNING: IsavedSP was null for frame " + this);
        }
      } else {
        interpreterSPAdjustmentOffset = savedSP.minus(getSP());
      }
    } else {
      interpreterSPAdjustmentOffset = 0;
    }
    if ( pc != null) {
      CodeBlob cb = VM.getVM().getCodeCache().findBlob(pc);
      if (cb != null && cb.isJavaMethod()) {
        NMethod nm = (NMethod) cb;
        if (pc.equals(nm.deoptBegin())) {
          pc = this.getUnextendedSP().getAddressAt(nm.origPCOffset());
          deoptimized = true;
        }
      }
    }
  }
  public SPARCFrame(Address raw_sp, Address pc) {
    super();
    if (DEBUG) {
      System.out.println("Constructing frame(2) raw_sp: " + raw_sp );
    }
    this.raw_sp = raw_sp;
    if (Assert.ASSERTS_ENABLED) {
      Assert.that((unBiasSP(raw_sp).andWithMask(VM.getVM().getAddressSize() - 1) == null),
                   "Expected raw sp likely got real sp, value was " + raw_sp);
    }
    raw_youngerSP = null;
    this.pc = pc;
    interpreterSPAdjustmentOffset = 0;
  }
  private SPARCFrame() {
  }
  public Object clone() {
    SPARCFrame frame = new SPARCFrame();
    frame.raw_sp = raw_sp;
    frame.pc = pc;
    frame.raw_youngerSP = raw_youngerSP;
    frame.interpreterSPAdjustmentOffset = interpreterSPAdjustmentOffset;
    frame.deoptimized = deoptimized;
    return frame;
  }
  public boolean equals(Object arg) {
    if (arg == null) {
      return false;
    }
    if (!(arg instanceof SPARCFrame)) {
      return false;
    }
    SPARCFrame other = (SPARCFrame) arg;
    return (AddressOps.equal(getSP(), other.getSP()) &&
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
    Address fp = getFP();
    Address sp = getSP();
    Address youngerSP = getYoungerSP();
    return "sp: " + (sp == null? "null" : sp.toString()) +
         ", younger_sp: " + (youngerSP == null? "null" : youngerSP.toString()) +
         ", fp: " + (fp == null? "null" : fp.toString()) +
         ", pc: " + (pc == null? "null" : pc.toString());
  }
  public boolean isSignalHandlerFrameDbg() {
    CDebugger cdbg = VM.getVM().getDebugger().getCDebugger();
    if (cdbg != null) {
      LoadObject dso = cdbg.loadObjectContainingPC(getPC());
      if (dso != null) {
        ClosestSymbol cs = dso.closestSymbolToPC(getPC());
        if (cs != null && cs.getName().equals("__sighndlr")) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else {
      if (getYoungerSP() == null) {
        return false;
      }
      Address i2 = getSP().getAddressAt(SPARCRegisters.I2.spOffsetInSavedWindow());
      if (i2 == null) {
        return false;
      }
      Address fp = getFP();
      int MAJOR_HACK_OFFSET = 8;  
      boolean res = i2.equals(fp.addOffsetTo(VM.getVM().getAddressSize() * (REGISTER_SAVE_WORDS + MAJOR_HACK_OFFSET)));
      if (res) {
        Address sigInfoAddr = getSP().getAddressAt(SPARCRegisters.I5.spOffsetInSavedWindow());
        if (sigInfoAddr == null) {
          System.err.println("Frame with fp = " + fp + " looked like a signal handler frame but wasn't");
          res = false;
        }
      }
      return res;
    }
  }
  public int getSignalNumberDbg() {
    Address sigInfoAddr = getSP().getAddressAt(SPARCRegisters.I5.spOffsetInSavedWindow());
    return (int) sigInfoAddr.getCIntegerAt(0, 4, false);
  }
  public String getSignalNameDbg() {
    return POSIXSignals.getSignalName(getSignalNumberDbg());
  }
  public boolean isInterpretedFrameValid() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isInterpretedFrame(), "Not an interpreted frame");
    }
    if (getFP() == null || (getFP().andWithMask(2 * VM.getVM().getAddressSize() - 1)) != null) {
      return false;
    }
    if (getSP() == null || (getSP().andWithMask(2 * VM.getVM().getAddressSize() - 1)) != null) {
      return false;
    }
    if (getFP().addOffsetTo(INTERPRETER_FRAME_VM_LOCAL_WORDS * VM.getVM().getAddressSize()).lessThan(getSP())) {
      return false;
    }
    OopHandle methodHandle = addressOfInterpreterFrameMethod().getOopHandleAt(0);
    if (VM.getVM().getObjectHeap().isValidMethod(methodHandle) == false) {
      return false;
    }
    if (getFP().lessThanOrEqual(getSP())) {        
      return false;
    }
    if (getFP().minus(getSP()) > 4096 * VM.getVM().getAddressSize()) {  
      return false;
    }
    Address bcx =  addressOfInterpreterFrameBCX().getAddressAt(0);
    Method method;
    try {
       method = (Method) VM.getVM().getObjectHeap().newOop(methodHandle);
    } catch (UnknownOopException ex) {
       return false;
    }
    int  bci = bcpToBci(bcx, method);
    if (bci < 0) return false;
    return true;
  }
  public long frameSize() {
    return (getSenderSP().minus(getSP()) / VM.getVM().getAddressSize());
  }
  public Address getLink() {
    return unBiasSP(getFP().getAddressAt(SPARCRegisters.FP.spOffsetInSavedWindow()));
  }
  public Frame sender(RegisterMap regMap, CodeBlob cb) {
    SPARCRegisterMap map = (SPARCRegisterMap) regMap;
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map != null, "map must be set");
    }
    map.setIncludeArgumentOops(false);
    if (isEntryFrame()) {
      return senderForEntryFrame(map);
    }
    Address youngerSP = getSP();
    Address sp        = getSenderSP();
    boolean isInterpreted = false;
    if (VM.getVM().isDebugging()) {
      if (isSignalHandlerFrameDbg()) {
        if (DEBUG) {
          System.out.println("SPARCFrame.sender: found signal handler frame");
        }
        long offset = getMContextAreaOffsetInUContext();
        Address fp = sp;
        fp = fp.addOffsetTo(getUContextOffset() + getMContextAreaOffsetInUContext());
        int PC_OFFSET_IN_GREGSET = 1;
        int SP_OFFSET_IN_GREGSET = 17;
        raw_sp = fp.getAddressAt(VM.getVM().getAddressSize() * SP_OFFSET_IN_GREGSET);
        Address pc = fp.getAddressAt(VM.getVM().getAddressSize() * PC_OFFSET_IN_GREGSET);
        return new SPARCFrame(raw_sp, pc);
      }
    }
    if (!VM.getVM().isCore()) {
      if (VM.getVM().getInterpreter().contains(pc)) {
        isInterpreted = true;
        map.makeIntegerRegsUnsaved();
        map.shiftWindow(sp, youngerSP);
      } else {
        cb = VM.getVM().getCodeCache().findBlob(pc);
        if (cb != null) {
          if (cb.callerMustGCArguments(map.getThread())) {
            map.setIncludeArgumentOops(true);
          }
          map.shiftWindow(sp, youngerSP);
          if (map.getUpdateMap()) {
            if (cb.getOopMaps() != null) {
              OopMapSet.updateRegisterMap(this, cb, map, VM.getVM().isDebugging());
            }
          }
        }
      }
    } 
    return new SPARCFrame(biasSP(sp), biasSP(youngerSP), isInterpreted);
  }
  protected boolean hasSenderPD() {
    try {
      if (getSP() == null) {
        return false;
      }
      if ( unBiasSP(getSP().getAddressAt(SPARCRegisters.FP.spOffsetInSavedWindow())) == null ) {
        return false;
      }
      return true;
    } catch (RuntimeException e) {
      if (DEBUG) {
        System.out.println("Bad frame " + this);
      }
      throw e;
    }
  }
  public Address getSenderPC() {
    return addressOfI7().getAddressAt(0).addOffsetTo(PC_RETURN_OFFSET);
  }
  public Address getUnextendedSP() {
    return getSP().addOffsetTo(interpreterSPAdjustmentOffset);
  }
  public Address getSenderSP() {
    return getFP();
  }
  public SPARCFrame afterSave() {
    return new SPARCFrame(biasSP(getYoungerSP()), null);
  }
  public Address getFP() {
    Address sp = getSP();
    if (sp == null) {
      System.out.println("SPARCFrame.getFP(): sp == null");
    }
    Address fpAddr = sp.addOffsetTo(SPARCRegisters.FP.spOffsetInSavedWindow());
    try {
      Address fp = unBiasSP(fpAddr.getAddressAt(0));
      if (fp == null) {
        System.out.println("SPARCFrame.getFP(): fp == null (&fp == " + fpAddr + ")");
      }
      return fp;
    } catch (RuntimeException e) {
      System.out.println("SPARCFrame.getFP(): is bad (&fp == " + fpAddr + " sp = " + sp + ")");
      return null;
    }
  }
  private Address addressOfFPSlot(int index) {
    return getFP().addOffsetTo(index * VM.getVM().getAddressSize());
  }
  public static final int INTERPRETER_FRAME_D_SCRATCH_FP_OFFSET           = -2;
  public static final int INTERPRETER_FRAME_L_SCRATCH_FP_OFFSET           = -4;
  public static final int INTERPRETER_FRAME_PADDING_OFFSET                = -5;
  public static final int INTERPRETER_FRAME_MIRROR_OFFSET                 = -6;
  public static final int INTERPRETER_FRAME_VM_LOCALS_FP_OFFSET           = -6;
  public static final int INTERPRETER_FRAME_VM_LOCAL_WORDS                = -INTERPRETER_FRAME_VM_LOCALS_FP_OFFSET;
  public static final int INTERPRETER_FRAME_EXTRA_OUTGOING_ARGUMENT_WORDS = 2;
  public Address addressOfInterpreterFrameLocals() {
    return getSP().addOffsetTo(SPARCRegisters.Llocals.spOffsetInSavedWindow());
  }
  private Address addressOfInterpreterFrameBCX() {
    return getSP().addOffsetTo(SPARCRegisters.Lbcp.spOffsetInSavedWindow());
  }
  public int getInterpreterFrameBCI() {
    Address bcp = addressOfInterpreterFrameBCX().getAddressAt(0);
    OopHandle methodHandle = addressOfInterpreterFrameMethod().getOopHandleAt(0);
    Method method = (Method) VM.getVM().getObjectHeap().newOop(methodHandle);
    return bcpToBci(bcp, method);
  }
  public Address addressOfInterpreterFrameExpressionStack() {
    return addressOfInterpreterFrameMonitors().addOffsetTo(-1 * VM.getVM().getAddressSize());
  }
  public int getInterpreterFrameExpressionStackDirection() {
    return -1;
  }
  public Address addressOfInterpreterFrameTOS() {
    return getSP().getAddressAt(SPARCRegisters.Lesp.spOffsetInSavedWindow()).addOffsetTo(VM.getVM().getAddressSize());
  }
  public Address addressOfInterpreterFrameTOSAt(int slot) {
    return addressOfInterpreterFrameTOS().addOffsetTo(slot * VM.getVM().getAddressSize());
  }
  public Address getInterpreterFrameSenderSP() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isInterpretedFrame(), "interpreted frame expected");
    }
    return getFP();
  }
  private Address addressOfInterpreterFrameMonitors() {
    return getSP().addOffsetTo(SPARCRegisters.Lmonitors.spOffsetInSavedWindow()).getAddressAt(0);
  }
  public BasicObjectLock interpreterFrameMonitorBegin() {
    int roundedVMLocalWords = Bits.roundTo(INTERPRETER_FRAME_VM_LOCAL_WORDS, WORDS_PER_LONG);
    return new BasicObjectLock(addressOfFPSlot(-1 * roundedVMLocalWords));
  }
  public BasicObjectLock interpreterFrameMonitorEnd() {
    return new BasicObjectLock(addressOfInterpreterFrameMonitors());
  }
  public int interpreterFrameMonitorSize() {
    return Bits.roundTo(BasicObjectLock.size(), WORDS_PER_LONG * (int) VM.getVM().getAddressSize());
  }
  public Address addressOfInterpreterFrameMethod() {
    return getSP().addOffsetTo(SPARCRegisters.Lmethod.spOffsetInSavedWindow());
  }
  public Address addressOfInterpreterFrameCPCache() {
    return getSP().addOffsetTo(SPARCRegisters.LcpoolCache.spOffsetInSavedWindow());
  }
  public JavaCallWrapper getEntryFrameCallWrapper() {
    SPARCArgument link = new SPARCArgument(0, false);
    return (JavaCallWrapper) VMObjectFactory.newObject(JavaCallWrapper.class,
                                                       getSP().getAddressAt(link.asIn().asRegister().spOffsetInSavedWindow()));
  }
  protected Address addressOfSavedOopResult() {
    return addressOfO0();
  }
  protected Address addressOfSavedReceiver() {
    return addressOfO0();
  }
  private Address addressOfI7() {
    return getSP().addOffsetTo(SPARCRegisters.I7.spOffsetInSavedWindow());
  }
  private Address addressOfO7() {
    return afterSave().addressOfI7();
  }
  private Address addressOfI0() {
    return getSP().addOffsetTo(SPARCRegisters.I0.spOffsetInSavedWindow());
  }
  private Address addressOfO0() {
    return afterSave().addressOfI0();
  }
  private static boolean addressesEqual(Address a1, Address a2) {
    if ((a1 == null) && (a2 == null)) {
      return true;
    }
    if ((a1 == null) || (a2 == null)) {
      return false;
    }
    return (a1.equals(a2));
  }
  private Frame senderForEntryFrame(RegisterMap regMap) {
    SPARCRegisterMap map = (SPARCRegisterMap) regMap;
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map != null, "map must be set");
    }
    JavaCallWrapper jcw = getEntryFrameCallWrapper();
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(!entryFrameIsFirst(), "next Java fp must be non zero");
      Assert.that(jcw.getLastJavaSP().greaterThan(getSP()), "must be above this frame on stack");
    }
    Address lastJavaSP = jcw.getLastJavaSP();
    Address lastJavaPC = jcw.getLastJavaPC();
    map.clear();
    if (!VM.getVM().isCore()) {
      map.makeIntegerRegsUnsaved();
      map.shiftWindow(lastJavaSP, null);
    }
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map.getIncludeArgumentOops(), "should be set by clear");
    }
    if (lastJavaPC != null) {
      return new SPARCFrame(biasSP(lastJavaSP), lastJavaPC);
    } else {
      Address youngerSP  = getNextYoungerSP(lastJavaSP, getSP());
      return new SPARCFrame(biasSP(lastJavaSP), biasSP(youngerSP), false);
    }
  }
  private static Address getNextYoungerSP(Address oldSP, Address youngSP) {
    Address sp = getNextYoungerSPOrNull(oldSP, youngSP, null);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(sp != null, "missed the SP");
    }
    return sp;
  }
  private static Address getNextYoungerSPOrNull(Address oldSP, Address youngSP, Address sp) {
    if (youngSP == null) {
      throw new RuntimeException("can not handle null youngSP in debugging system (seems to require register window flush)");
    }
    if (sp == null) {
      sp = youngSP;
    }
    Address previousSP = null;
    int maxFrames = (int) (oldSP.minus(sp) / (16 * VM.getVM().getAddressSize()));
    while(!sp.equals(oldSP) && spIsValid(oldSP, youngSP, sp)) {
      if (maxFrames-- <= 0) {
        break;
      }
      previousSP = sp;
      sp = unBiasSP(sp.getAddressAt(SPARCRegisters.FP.spOffsetInSavedWindow()));
    }
    return (sp.equals(oldSP) ? previousSP : null);
  }
  private static boolean spIsValid(Address oldSP, Address youngSP, Address sp) {
    long mask = VM.getVM().getAddressSize();
    mask = 2 * mask - 1;
    return ((sp.andWithMask(mask) == null) &&
            (sp.lessThanOrEqual(oldSP)) &&
            (sp.greaterThanOrEqual(youngSP)));
  }
  public long getUContextOffset() {
    int MAJOR_HACK_OFFSET = 8;
    return VM.getVM().getAddressSize() * (REGISTER_SAVE_WORDS + MAJOR_HACK_OFFSET);
  }
  public long getMContextAreaOffsetInUContext() {
    long offset = VM.getVM().alignUp(4, VM.getVM().getAddressSize());   
    offset      = VM.getVM().alignUp(offset + VM.getVM().getAddressSize(), 8); 
    offset     += 16 +                                                  
                   2 * VM.getVM().getAddressSize() + 4;                 
    offset      = VM.getVM().alignUp(offset + VM.getVM().getAddressSize(), 8); 
    return offset;
  }
}
