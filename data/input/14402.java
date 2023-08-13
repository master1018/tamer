public class VFrame {
  protected Frame       fr;
  protected RegisterMap regMap;
  protected JavaThread  thread;
  protected VFrame(Frame f, RegisterMap regMap, JavaThread thread) {
    this.regMap = (RegisterMap) regMap.clone();
    if (f != null) {
      fr = (Frame) f.clone();
    }
    this.thread = thread;
  }
  public static VFrame newVFrame(Frame f, RegisterMap regMap, JavaThread thread, boolean unsafe, boolean mayBeImprecise) {
    if (f.isInterpretedFrame()) {
      return new InterpretedVFrame(f, regMap, thread);
    }
    if (!VM.getVM().isCore()) {
      CodeBlob cb;
      if (unsafe) {
        cb = VM.getVM().getCodeCache().findBlobUnsafe(f.getPC());
      } else {
        cb = VM.getVM().getCodeCache().findBlob(f.getPC());
      }
      if (cb != null) {
        if (cb.isNMethod()) {
          NMethod nm = (NMethod) cb;
          ScopeDesc scope = null;
          if (mayBeImprecise || VM.getVM().isDebugging()) {
            scope = nm.getScopeDescNearDbg(f.getPC());
          } else {
            scope = nm.getScopeDescAt(f.getPC());
          }
          return new CompiledVFrame(f, regMap, thread, scope, mayBeImprecise);
        }
        if (f.isGlueFrame()) {
          RegisterMap tempMap = regMap.copy();
          Frame s = f.sender(tempMap);
          return newVFrame(s, tempMap, thread, unsafe, false);
        }
      }
    }
    return new ExternalVFrame(f, regMap, thread, mayBeImprecise);
  }
  public static VFrame newVFrame(Frame f, RegisterMap regMap, JavaThread thread) {
    return newVFrame(f, regMap, thread, false, false);
  }
  public Frame       getFrame()       { return fr;     }
  public RegisterMap getRegisterMap() { return regMap; }
  public JavaThread  getThread()      { return thread; }
  public VFrame sender() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isTop(), "just checking");
    }
    return sender(false);
  }
  public VFrame sender(boolean mayBeImprecise) {
    RegisterMap tempMap = (RegisterMap) getRegisterMap().clone();
    if (fr.isFirstFrame()) {
      return null;
    }
    Frame s = fr.realSender(tempMap);
    if (s == null) {
      Assert.that(VM.getVM().getCPU().equals("ia64"), "Only ia64 should have null here");
      return null;
    }
    if (s.isFirstFrame()) {
      return null;
    }
    return VFrame.newVFrame(s, tempMap, getThread(), VM.getVM().isDebugging(), mayBeImprecise);
  }
  public JavaVFrame javaSender() {
    boolean imprecise = false;
    if (VM.getVM().isDebugging()) {
      if (!isJavaFrame()) {
        imprecise = mayBeImpreciseDbg();
      }
    }
    VFrame f = sender(imprecise);
    while (f != null) {
      if (f.isJavaFrame()) {
        return (JavaVFrame) f;
      }
      f = f.sender(imprecise);
    }
    return null;
  }
  public boolean isTop() {
    return true;
  }
  public VFrame top() {
    VFrame vf = this;
    while (!vf.isTop()) {
      vf = vf.sender();
    }
    return vf;
  }
  public boolean isEntryFrame()       { return false; }
  public boolean isJavaFrame()        { return false; }
  public boolean isInterpretedFrame() { return false; }
  public boolean isCompiledFrame()    { return false; }
  public boolean isDeoptimized()      { return false; }
  public boolean mayBeImpreciseDbg()  { return false; }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    if (VM.getVM().wizardMode()) {
      fr.printValueOn(tty);
    }
  }
  public void printValue() {
    printValueOn(System.out);
  }
  public void printValueOn(PrintStream tty) {
    printOn(tty);
  }
}
