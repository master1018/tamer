public class PCFinder {
  public static final int LOW_CONFIDENCE = 1;
  public static final int HIGH_CONFIDENCE = 2;
  public static class Info {
    private String name;
    private long   offset;
    private int    confidence;
    public Info(String name, long offset, int confidence) {
      this.name = name;
      this.offset = offset;
      this.confidence = confidence;
    }
    public String getName()       { return name;       }
    public long   getOffset()     { return offset;     }
    public int    getConfidence() { return confidence; }
  }
  public static Info findPC(Address pc, LoadObject lo, CDebugger dbg) {
    if (lo == null) {
      return new Info(null, -1, LOW_CONFIDENCE);
    }
    BlockSym sym = lo.debugInfoForPC(pc);
    while (sym != null) {
      if (sym.isFunction()) {
        return new Info(sym.toString(), pc.minus(sym.getAddress()), HIGH_CONFIDENCE);
      }
    }
    int confidence = HIGH_CONFIDENCE;
    ClosestSymbol cs = lo.closestSymbolToPC(pc);
    if (cs != null) {
      return new Info(cs.getName() + "()", cs.getOffset(), LOW_CONFIDENCE);
    }
    return new Info(dbg.getNameOfFile(lo.getName()).toUpperCase() +
                    "! " + pc + "()", -1, HIGH_CONFIDENCE);
  }
}
