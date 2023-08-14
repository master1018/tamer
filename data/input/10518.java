public class IA64Frame extends Frame {
  private static final boolean DEBUG = false;
  Address iframe;
  private IA64Frame() {
  }
  public IA64Frame(Address raw_sp, Address iframe, Address pc) {
    this.raw_sp = raw_sp;
    this.iframe = iframe;
    this.pc = pc;
    if (DEBUG) {
      System.err.println("IA64Frame(sp, iframe, pc): " + this);
      dumpStack();
    }
  }
  public Object clone() {
    IA64Frame frame = new IA64Frame();
    frame.raw_sp = raw_sp;
    frame.iframe = iframe;
    frame.pc = pc;
    return frame;
  }
  public boolean equals(Object arg) {
    if (arg == null) {
      return false;
    }
    if (!(arg instanceof IA64Frame)) {
      return false;
    }
    IA64Frame other = (IA64Frame) arg;
    return (AddressOps.equal(getSP(), other.getSP()) &&
            AddressOps.equal(getIFRAME(), other.getIFRAME()) &&
            AddressOps.equal(getPC(), other.getPC()));
  }
  public int hashCode() {
    if (iframe == null) {
      return 0;
    }
    return iframe.hashCode();
  }
  public String toString() {
    return "sp: " + (getSP() == null? "null" : getSP().toString()) +
         ", iframe: " + (getIFRAME() == null? "null" : getIFRAME().toString()) +
         ", pc: " + (pc == null? "null" : pc.toString());
  }
  public Address getFP() { return null; }
  public Address getIFRAME() { return iframe; }
  public Address getSP() { return raw_sp; }
  public Address getID() { return getFP(); }
  public boolean isSignalHandlerFrameDbg() { return false; }
  public int     getSignalNumberDbg()      { return 0;     }
  public String  getSignalNameDbg()        { return null;  }
  public boolean isInterpretedFrameValid() {
    return true;
  }
  public boolean isInterpretedFrame() { return iframe != null; }
  public Frame sender(RegisterMap regMap, CodeBlob cb) {
    if (iframe == null) {
      return null;
    }
    cInterpreter fr = new cInterpreter(iframe);
    if (fr.prev() == null) {
      Address wrapper = fr.wrapper();
      if ( wrapper == null) {
        return null;
      }
      IA64JavaCallWrapper jcw = new IA64JavaCallWrapper(wrapper);
      Address iprev = jcw.getPrevIFrame();
      if (iprev == null) {
        return null;
      }
      return new IA64Frame(null, iprev, null);
    } else {
      return new IA64Frame(null, fr.prev(), null);
    }
  }
  private Frame senderForEntryFrame(IA64RegisterMap map) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map != null, "map must be set");
    }
    throw new RuntimeException("senderForEntryFrame NYI");
  }
  private Frame senderForInterpreterFrame(IA64RegisterMap map) {
    throw new RuntimeException("senderForInterpreterFrame NYI");
  }
  private Frame senderForDeoptimizedFrame(IA64RegisterMap map, CodeBlob cb) {
    throw new RuntimeException("Deoptimized frames not handled yet");
  }
  private Frame senderForCompiledFrame(IA64RegisterMap map, CodeBlob cb) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(map != null, "map must be set");
    }
    throw new RuntimeException("senderForCompiledFrame NYI");
  }
  protected boolean hasSenderPD() {
    return true;
  }
  public long frameSize() {
    throw new RuntimeException("frameSize NYI");
  }
  public Address getLink() {
    throw new RuntimeException("getLink NYI");
  }
  public Address getUnextendedSP() { return getSP(); }
  public Address getSenderPC()     { return null;  }
  public Address getSenderSP()     { return null; }
  public Address addressOfInterpreterFrameLocals() {
    if (iframe == null) {
      throw new RuntimeException("Not an Interpreter frame");
    }
    cInterpreter fr = new cInterpreter(iframe);
    return fr.locals();
  }
  private Address addressOfInterpreterFrameBCX() {
    if (iframe == null) {
      throw new RuntimeException("Not an Interpreter frame");
    }
    cInterpreter fr = new cInterpreter(iframe);
    return fr.bcpAddr();
  }
  public int getInterpreterFrameBCI() {
    Address bcp = addressOfInterpreterFrameBCX().getAddressAt(0);
    OopHandle methodHandle = addressOfInterpreterFrameMethod().getOopHandleAt(0);
    Method method = (Method) VM.getVM().getObjectHeap().newOop(methodHandle);
    return bcpToBci(bcp, method);
  }
  public Address addressOfInterpreterFrameMDX() {
    return null;
  }
  public Address addressOfInterpreterFrameExpressionStack() {
    if (iframe == null) {
      throw new RuntimeException("Not an Interpreter frame");
    }
    cInterpreter fr = new cInterpreter(iframe);
    return fr.stackBase();
  }
  public int getInterpreterFrameExpressionStackDirection() { return -1; }
  public Address addressOfInterpreterFrameTOS() {
    if (iframe == null) {
      throw new RuntimeException("Not an Interpreter frame");
    }
    cInterpreter fr = new cInterpreter(iframe);
    return fr.stackBase().addOffsetTo(VM.getVM().getAddressSize());
  }
  public Address addressOfInterpreterFrameTOSAt(int slot) {
    return addressOfInterpreterFrameTOS().addOffsetTo(slot * VM.getVM().getAddressSize());
  }
  public Address getInterpreterFrameSenderSP() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isInterpretedFrame(), "interpreted frame expected");
    }
    throw new RuntimeException("getInterpreterFrameSenderSP NYI");
  }
  public BasicObjectLock interpreterFrameMonitorBegin() {
    if (iframe == null) {
      throw new RuntimeException("Not an Interpreter frame");
    }
    cInterpreter fr = new cInterpreter(iframe);
    return new BasicObjectLock(fr.monitorBase());
  }
  public BasicObjectLock interpreterFrameMonitorEnd() {
    if (iframe == null) {
      throw new RuntimeException("Not an Interpreter frame");
    }
    cInterpreter fr = new cInterpreter(iframe);
    Address result = fr.stackBase().addOffsetTo(2 * VM.getVM().getAddressSize());
    return new BasicObjectLock(result);
  }
  public int interpreterFrameMonitorSize() {
    return BasicObjectLock.size();
  }
  public Address addressOfInterpreterFrameMethod() {
    if (iframe == null) {
      throw new RuntimeException("Not an Interpreter frame");
    }
    cInterpreter fr = new cInterpreter(iframe);
    return fr.methodAddr();
  }
  public Address addressOfInterpreterFrameCPCache() {
    if (iframe == null) {
      throw new RuntimeException("Not an Interpreter frame");
    }
    cInterpreter fr = new cInterpreter(iframe);
    return fr.constantsAddr();
  }
  public JavaCallWrapper getEntryFrameCallWrapper() {
    throw new RuntimeException("getEntryFrameCallWrapper NYI");
  }
  protected Address addressOfSavedOopResult() {
    throw new RuntimeException("public boolean isInterpretedFrame() NYI");
  }
  protected Address addressOfSavedReceiver() {
    throw new RuntimeException("getEntryFrameCallWrapper NYI");
  }
  private void dumpStack() {
  }
}
