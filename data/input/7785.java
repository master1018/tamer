public class ScopeDesc {
  private NMethod code;
  private Method  method;
  private int     bci;
  private boolean reexecute;
  private int     decodeOffset;
  private int     senderDecodeOffset;
  private int     localsDecodeOffset;
  private int     expressionsDecodeOffset;
  private int     monitorsDecodeOffset;
  private List    objects; 
  private ScopeDesc(NMethod code, int decodeOffset, List objects, boolean reexecute) {
    this.code = code;
    this.decodeOffset = decodeOffset;
    this.objects      = objects;
    this.reexecute    = reexecute;
    DebugInfoReadStream stream  = streamAt(decodeOffset);
    senderDecodeOffset = stream.readInt();
    method = (Method) VM.getVM().getObjectHeap().newOop(stream.readOopHandle());
    bci    = stream.readBCI();
    localsDecodeOffset      = stream.readInt();
    expressionsDecodeOffset = stream.readInt();
    monitorsDecodeOffset    = stream.readInt();
  }
  public ScopeDesc(NMethod code, int decodeOffset, int objectDecodeOffset, boolean reexecute) {
    this.code = code;
    this.decodeOffset = decodeOffset;
    this.objects      = decodeObjectValues(objectDecodeOffset);
    this.reexecute    = reexecute;
    DebugInfoReadStream stream  = streamAt(decodeOffset);
    senderDecodeOffset = stream.readInt();
    method = (Method) VM.getVM().getObjectHeap().newOop(stream.readOopHandle());
    bci    = stream.readBCI();
    localsDecodeOffset      = stream.readInt();
    expressionsDecodeOffset = stream.readInt();
    monitorsDecodeOffset    = stream.readInt();
  }
  public NMethod getNMethod()   { return code; }
  public Method getMethod()     { return method; }
  public int    getBCI()        { return bci;    }
  public boolean getReexecute() { return reexecute;}
  public List getLocals() {
    return decodeScopeValues(localsDecodeOffset);
  }
  public List getExpressions() {
    return decodeScopeValues(expressionsDecodeOffset);
  }
  public List getMonitors() {
    return decodeMonitorValues(monitorsDecodeOffset);
  }
  public List getObjects() {
    return objects;
  }
  public ScopeDesc sender() {
    if (isTop()) {
      return null;
    }
    return new ScopeDesc(code, senderDecodeOffset, objects, false);
  }
  public int getDecodeOffset() {
    return decodeOffset;
  }
  public boolean isTop() {
    return (senderDecodeOffset == DebugInformationRecorder.SERIALIZED_NULL);
  }
  public boolean equals(Object arg) {
    if (arg == null) {
      return false;
    }
    if (!(arg instanceof ScopeDesc)) {
      return false;
    }
    ScopeDesc sd = (ScopeDesc) arg;
    return (sd.method.equals(method) && (sd.bci == bci));
  }
  public void printValue() {
    printValueOn(System.out);
  }
  public void printValueOn(PrintStream tty) {
    tty.print("ScopeDesc for ");
    method.printValueOn(tty);
    tty.print(" @bci " + bci);
    tty.println(" reexecute=" + reexecute);
  }
  private DebugInfoReadStream streamAt(int decodeOffset) {
    return new DebugInfoReadStream(code, decodeOffset, objects);
  }
  private List decodeScopeValues(int decodeOffset) {
    if (decodeOffset == DebugInformationRecorder.SERIALIZED_NULL) {
      return null;
    }
    DebugInfoReadStream stream = streamAt(decodeOffset);
    int length = stream.readInt();
    List res = new ArrayList(length);
    for (int i = 0; i < length; i++) {
      res.add(ScopeValue.readFrom(stream));
    }
    return res;
  }
  private List decodeMonitorValues(int decodeOffset) {
    if (decodeOffset == DebugInformationRecorder.SERIALIZED_NULL) {
      return null;
    }
    DebugInfoReadStream stream = streamAt(decodeOffset);
    int length = stream.readInt();
    List res = new ArrayList(length);
    for (int i = 0; i < length; i++) {
      res.add(new MonitorValue(stream));
    }
    return res;
  }
  private List decodeObjectValues(int decodeOffset) {
    if (decodeOffset == DebugInformationRecorder.SERIALIZED_NULL) {
      return null;
    }
    List res = new ArrayList();
    DebugInfoReadStream stream = new DebugInfoReadStream(code, decodeOffset, res);
    int length = stream.readInt();
    for (int i = 0; i < length; i++) {
      ScopeValue.readFrom(stream);
    }
    Assert.that(res.size() == length, "inconsistent debug information");
    return res;
  }
}
