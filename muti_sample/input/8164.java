public class BytecodeStream {
  private Method _method;
  private int     _bci;       
  private int     _next_bci;  
  private int     _end_bci;   
  private int     _code;
  private boolean _is_wide;
  public BytecodeStream(Method method) {
    _method = method;
    setInterval(0, (int) method.getCodeSize());
  }
  public void setInterval(int beg_bci, int end_bci) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(0 <= beg_bci && beg_bci <= _method.getCodeSize(), "illegal beg_bci");
      Assert.that(0 <= end_bci && end_bci <= _method.getCodeSize(), "illegal end_bci");
    }
    _bci      = beg_bci;
    _next_bci = beg_bci;
    _end_bci  = end_bci;
  }
  public void setStart(int beg_bci) {
    setInterval(beg_bci, (int) _method.getCodeSize());
  }
  public int next() {
    int code;
    _bci = _next_bci;
    if (isLastBytecode()) {
      code = Bytecodes._illegal;
    } else {
      int rawCode = Bytecodes.codeAt(_method, _bci);
      code = 0; 
      try {
        code = Bytecodes.javaCode(rawCode);
      } catch (AssertionFailure e) {
        e.printStackTrace();
        Assert.that(false, "Failure occurred at bci " + _bci + " in method " + _method.externalNameAndSignature());
      }
      int l = Bytecodes.lengthFor(code);
      if (l == 0) l = Bytecodes.lengthAt(_method, _bci);
      _next_bci  += l;
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(_bci < _next_bci, "length must be > 0");
      }
      _is_wide      = false;
      if (code == Bytecodes._wide) {
        code = _method.getBytecodeOrBPAt(_bci + 1);
        _is_wide = true;
      }
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(Bytecodes.isJavaCode(code), "sanity check");
      }
    }
    _code = code;
    return _code;
  }
  public Method  method()             { return _method; }
  public int     bci()                { return _bci; }
  public int     nextBCI()            { return _next_bci; }
  public int     endBCI()             { return _end_bci; }
  public int     code()               { return _code; }
  public boolean isWide()             { return _is_wide; }
  public boolean isActiveBreakpoint() { return Bytecodes.isActiveBreakpointAt(_method, _bci); }
  public boolean isLastBytecode()     { return _next_bci >= _end_bci; }
  public void    setNextBCI(int bci)  {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(0 <= bci && bci <= _method.getCodeSize(), "illegal bci");
    }
    _next_bci = bci;
  }
  public int     dest()               { return bci() + _method.getBytecodeShortArg(bci() + 1); }
  public int     dest_w()             { return bci() + _method.getBytecodeIntArg(bci()   + 1); }
  public int     getIndex()           { return (isWide())
                                          ? (_method.getBytecodeShortArg(bci() + 2) & 0xFFFF)
                                          : (_method.getBytecodeOrBPAt(bci() + 1) & 0xFF); }
  public int     getIndexBig()        { return _method.getBytecodeShortArg(bci() + 1); }
  public int     codeAt(int bci) {
    return _method.getBytecodeOrBPAt(bci);
  }
}
