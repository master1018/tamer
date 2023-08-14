public class CompiledVFrame extends JavaVFrame {
  private ScopeDesc scope;
  private boolean mayBeImprecise;
  public CompiledVFrame(Frame fr, RegisterMap regMap, JavaThread thread, ScopeDesc scope, boolean mayBeImprecise) {
    super(fr, regMap, thread);
    this.scope = scope;
    this.mayBeImprecise = mayBeImprecise;
    if (!VM.getVM().isDebugging()) {
      Assert.that(scope != null, "scope must be present");
    }
  }
  public boolean isTop() {
    if (VM.getVM().isDebugging()) {
      return (getScope() == null || getScope().isTop());
    } else {
      return getScope().isTop();
    }
  }
  public boolean isCompiledFrame() {
    return true;
  }
  public boolean isDeoptimized() {
    return fr.isDeoptimized();
  }
  public boolean mayBeImpreciseDbg() {
    return mayBeImprecise;
  }
  public NMethod getCode() {
    return VM.getVM().getCodeCache().findNMethod(fr.getPC());
  }
  public NMethod getCodeUnsafe() {
    return VM.getVM().getCodeCache().findNMethodUnsafe(fr.getPC());
  }
  public ScopeDesc getScope() {
    return scope;
  }
  public Method getMethod() {
    if (VM.getVM().isDebugging() && getScope() == null) {
      return getCodeUnsafe().getMethod();
    }
    return getScope().getMethod();
  }
  public StackValueCollection getLocals() {
    List scvList = getScope().getLocals();
    if (scvList == null)
      return new StackValueCollection();
    int length = scvList.size();
    StackValueCollection result = new StackValueCollection(length);
    for( int i = 0; i < length; i++ )
      result.add( createStackValue((ScopeValue) scvList.get(i)) );
    return result;
  }
  public StackValueCollection getExpressions() {
    List scvList = getScope().getExpressions();
    if (scvList == null)
      return new StackValueCollection();
    int length = scvList.size();
    StackValueCollection result = new StackValueCollection(length);
    for( int i = 0; i < length; i++ )
      result.add( createStackValue((ScopeValue) scvList.get(i)) );
    return result;
  }
  public List   getMonitors() {
    List monitors = getScope().getMonitors();
    if (monitors == null) {
      return new ArrayList();
    }
    List result = new ArrayList(monitors.size());
    for (int i = 0; i < monitors.size(); i++) {
      MonitorValue mv = (MonitorValue) monitors.get(i);
      ScopeValue ov = mv.owner();
      StackValue ownerSV = createStackValue(ov); 
      if (ov.isObject()) { 
        Assert.that(mv.eliminated() && ownerSV.objIsScalarReplaced(), "monitor should be eliminated for scalar replaced object");
        ScopeValue kv = ((ObjectValue)ov).getKlass();
        Assert.that(kv.isConstantOop(), "klass should be oop constant for scalar replaced object");
        OopHandle k = ((ConstantOopReadValue)kv).getValue();
        result.add(new MonitorInfo(k, resolveMonitorLock(mv.basicLock()), mv.eliminated(), true));
      } else {
        result.add(new MonitorInfo(ownerSV.getObject(), resolveMonitorLock(mv.basicLock()), mv.eliminated(), false));
      }
    }
    return result;
  }
  public int getBCI() {
    int raw = getRawBCI();
    return ((raw == DebugInformationRecorder.SYNCHRONIZATION_ENTRY_BCI) ? 0 : raw);
  }
  public int getRawBCI() {
    if (VM.getVM().isDebugging() && getScope() == null) {
      return 0; 
    }
    return getScope().getBCI();
  }
  public VFrame sender() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isTop(), "just checking");
    }
    return sender(false);
  }
  public VFrame sender(boolean mayBeImprecise) {
    if (!VM.getVM().isDebugging()) {
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(scope != null, "When new stub generator is in place, then scope can never be NULL");
      }
    }
    Frame f = (Frame) getFrame().clone();
    return (isTop()
              ? super.sender(false)
              : new CompiledVFrame(f, getRegisterMap(), getThread(), getScope().sender(), mayBeImprecise));
  }
  private StackValue createStackValue(ScopeValue sv) {
    if (sv.isLocation()) {
      Location loc = ((LocationValue) sv).getLocation();
      if (loc.isIllegal()) return new StackValue();
      Address valueAddr = loc.isRegister()
        ? getRegisterMap().getLocation(new VMReg(loc.getRegisterNumber()))
        : ((Address)fr.getUnextendedSP()).addOffsetTo(loc.getStackOffset());
      if (loc.holdsFloat()) {    
        if (Assert.ASSERTS_ENABLED) {
          Assert.that( loc.isRegister(), "floats always saved to stack in 1 word" );
        }
        float value = (float) valueAddr.getJDoubleAt(0);
        return new StackValue(Float.floatToIntBits(value) & 0xFFFFFFFF); 
      } else if (loc.holdsInt()) {  
        if (Assert.ASSERTS_ENABLED) {
          Assert.that( loc.isRegister(), "ints always saved to stack in 1 word" );
        }
        return new StackValue(valueAddr.getJLongAt(0) & 0xFFFFFFFF);
      } else if (loc.holdsNarrowOop()) {  
        if (loc.isRegister() && VM.getVM().isBigEndian()) {
          return new StackValue(valueAddr.getCompOopHandleAt(VM.getVM().getIntSize()), 0);
        } else {
          return new StackValue(valueAddr.getCompOopHandleAt(0), 0);
        }
      } else if( loc.holdsOop() ) {  
        return new StackValue(valueAddr.getOopHandleAt(0), 0);
      } else if( loc.holdsDouble() ) {
        return new StackValue(valueAddr.getJIntAt(0) & 0xFFFFFFFF);
      } else if(loc.holdsAddr()) {
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(!VM.getVM().isServerCompiler(), "No address type for locations with C2 (jsr-s are inlined)");
        }
        return new StackValue(0);
      } else if (VM.getVM().isLP64() && loc.holdsLong()) {
        if ( loc.isRegister() ) {
          return new StackValue(((valueAddr.getJLongAt(0) & 0xFFFFFFFF) << 32) |
                                ((valueAddr.getJLongAt(8) & 0xFFFFFFFF)));
        } else {
          return new StackValue(valueAddr.getJLongAt(0));
        }
      } else if( loc.isRegister() ) {
        return new StackValue(valueAddr.getJIntAt(0) & 0xFFFFFFFF);
      } else {
        return new StackValue(valueAddr.getJIntAt(0) & 0xFFFFFFFF);
      }
    } else if (sv.isConstantInt()) {
      return new StackValue(((ConstantIntValue) sv).getValue() & 0xFFFFFFFF);
    } else if (sv.isConstantOop()) {
      return new StackValue(((ConstantOopReadValue) sv).getValue(), 0);
    } else if (sv.isConstantDouble()) {
      double d = ((ConstantDoubleValue) sv).getValue();
      return new StackValue(Double.doubleToLongBits(d) & 0xFFFFFFFF);
    } else if (VM.getVM().isLP64() && sv.isConstantLong()) {
      return new StackValue(((ConstantLongValue) sv).getValue() & 0xFFFFFFFF);
    } else if (sv.isObject()) {
      return new StackValue(((ObjectValue)sv).getValue(), 1);
    }
    Assert.that(false, "Should not reach here");
    return new StackValue(0);   
  }
  private BasicLock resolveMonitorLock(Location location) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(location.isStack(), "for now we only look at the stack");
    }
    int byteOffset = location.getStackOffset();
    return new BasicLock(getFrame().getUnextendedSP().addOffsetTo(byteOffset));
  }
}
