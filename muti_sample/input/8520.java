public class InterpretedVFrame extends JavaVFrame {
  public Method getMethod() {
    return getFrame().getInterpreterFrameMethod();
  }
  public StackValueCollection getLocals() {
    Method m = getMethod();
    int length = (int) m.getMaxLocals();
    if (m.isNative()) {
      length = (int) m.getSizeOfParameters();
    }
    StackValueCollection result = new StackValueCollection(length);
    OopMapCacheEntry oopMask = getMethod().getMaskFor(getBCI());
    for(int i = 0; i < length; i++) {
      Address addr = addressOfLocalAt(i);
      StackValue sv;
      if (oopMask.isOop(i)) {
        sv = new StackValue(addr.getOopHandleAt(0), 0);
      } else {
        sv = new StackValue(addr.getCIntegerAt(0, VM.getVM().getAddressSize(), false));
      }
      result.add(sv);
    }
    return result;
  }
  public StackValueCollection getExpressions() {
    int length = getFrame().getInterpreterFrameExpressionStackSize();
    if (getMethod().isNative()) {
      length = 0;
    }
    int nofLocals = (int) getMethod().getMaxLocals();
    StackValueCollection result = new StackValueCollection(length);
    OopMapCacheEntry oopMask = getMethod().getMaskFor(getBCI());
    for(int i = 0; i < length; i++) {
      Address addr = addressOfExpressionStackAt(i);
      StackValue sv;
      if (oopMask.isOop(i + nofLocals)) {
        sv = new StackValue(addr.getOopHandleAt(0), 0);
      } else {
        sv = new StackValue(addr.getCIntegerAt(0, VM.getVM().getAddressSize(), false));
      }
      result.add(sv);
    }
    return result;
  }
  public List   getMonitors() {
    List result = new ArrayList(5);
    for (BasicObjectLock current = getFrame().interpreterFrameMonitorEnd();
         current.address().lessThan(getFrame().interpreterFrameMonitorBegin().address());
         current = getFrame().nextMonitorInInterpreterFrame(current)) {
      result.add(new MonitorInfo(current.obj(), current.lock(), false, false));
    }
    return result;
  }
  public boolean isInterpretedFrame() { return true; }
  InterpretedVFrame(Frame fr, RegisterMap regMap, JavaThread thread) {
    super(fr, regMap, thread);
  }
  public int getBCI() {
    return getFrame().getInterpreterFrameBCI();
  }
  public void verify() {
  }
  private Address addressOfLocalAt(int index) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(getFrame().isInterpretedFrame(), "frame should be an interpreted frame");
    }
    return fr.addressOfInterpreterFrameLocal(index);
  }
  private Address addressOfExpressionStackAt(int index) {
    return fr.addressOfInterpreterFrameExpressionStackSlot(index);
  }
}
