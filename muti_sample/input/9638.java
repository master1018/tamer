public class BasicCDebugInfoDataBase implements CDebugInfoDataBase {
  private static final int INITIALIZED_STATE  = 0;
  private static final int CONSTRUCTION_STATE = 1;
  private static final int RESOLVED_STATE     = 2;
  private static final int COMPLETE_STATE     = 3;
  private int state = INITIALIZED_STATE;
  private Map lazyTypeMap;
  private List types;
  private Map nameToTypeMap;
  private Map lazySymMap;
  private List blocks;
  private Map nameToSymMap;
  private BasicLineNumberMapping lineNumbers;
  public void beginConstruction() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(state == INITIALIZED_STATE, "wrong state");
    }
    state   = CONSTRUCTION_STATE;
    lazyTypeMap  = new HashMap();
    types        = new ArrayList();
    lazySymMap   = new HashMap();
    blocks       = new ArrayList();
    nameToSymMap = new HashMap();
    lineNumbers  = new BasicLineNumberMapping();
  }
  public void addType(Object lazyKey, Type type) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(state == CONSTRUCTION_STATE, "wrong state");
    }
    if (lazyKey != null) {
      if (lazyTypeMap.put(lazyKey, type) != null) {
        throw new RuntimeException("Type redefined for lazy key " + lazyKey);
      }
    } else {
      types.add(type);
    }
  }
  public void resolve(ResolveListener listener) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(state == CONSTRUCTION_STATE, "wrong state");
    }
    resolveLazyMap(listener);
    for (ListIterator iter = types.listIterator(); iter.hasNext(); ) {
      BasicType t = (BasicType) iter.next();
      BasicType t2 = (BasicType) t.resolveTypes(this, listener);
      if (t != t2) {
        iter.set(t2);
      }
    }
    for (Iterator iter = blocks.iterator(); iter.hasNext(); ) {
      ((BasicSym) iter.next()).resolve(this, listener);
    }
    for (Iterator iter = nameToSymMap.values().iterator(); iter.hasNext(); ) {
      ((BasicSym) iter.next()).resolve(this, listener);
    }
    Collections.sort(blocks, new Comparator() {
        public int compare(Object o1, Object o2) {
          BlockSym b1 = (BlockSym) o1;
          BlockSym b2 = (BlockSym) o2;
          Address a1 = b1.getAddress();
          Address a2 = b2.getAddress();
          if (AddressOps.lt(a1, a2)) { return -1; }
          if (AddressOps.gt(a1, a2)) { return 1; }
          return 0;
        }
      });
    state = RESOLVED_STATE;
  }
  public void endConstruction() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(state == RESOLVED_STATE, "wrong state");
    }
    for (Iterator iter = lazyTypeMap.values().iterator(); iter.hasNext(); ) {
      types.add(iter.next());
    }
    nameToTypeMap = new HashMap();
    for (Iterator iter = types.iterator(); iter.hasNext(); ) {
      Type t = (Type) iter.next();
      if (!t.isConst() && !t.isVolatile()) {
        nameToTypeMap.put(t.getName(), t);
      }
    }
    lazyTypeMap = null;
    lazySymMap  = null;
    lineNumbers.sort();
    lineNumbers.recomputeEndPCs();
    state = COMPLETE_STATE;
  }
  public Type lookupType(String name) {
    return lookupType(name, 0);
  }
  public Type lookupType(String name, int cvAttributes) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(state == COMPLETE_STATE, "wrong state");
    }
    BasicType t = (BasicType) nameToTypeMap.get(name);
    if (t != null) {
      if (cvAttributes != 0) {
        t = (BasicType) t.getCVVariant(cvAttributes);
      }
    }
    return t;
  }
  public void iterate(TypeVisitor v) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(state == COMPLETE_STATE, "wrong state");
    }
    for (Iterator iter = types.iterator(); iter.hasNext(); ) {
      BasicType t = (BasicType) iter.next();
      t.visit(v);
    }
  }
  public void addBlock(Object key, BlockSym block) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(key != null, "key must be non-null");
    }
    lazySymMap.put(key, block);
    blocks.add(block);
  }
  public void addGlobalSym(GlobalSym sym) {
    nameToSymMap.put(sym.getName(), sym);
  }
  public BlockSym debugInfoForPC(Address pc) {
    return searchBlocks(pc, 0, blocks.size() - 1);
  }
  public GlobalSym lookupSym(String name) {
    return (GlobalSym) nameToSymMap.get(name);
  }
  public void addLineNumberInfo(BasicLineNumberInfo info) {
    lineNumbers.addLineNumberInfo(info);
  }
  public LineNumberInfo lineNumberForPC(Address pc) throws DebuggerException {
    return lineNumbers.lineNumberForPC(pc);
  }
  public void iterate(LineNumberVisitor v) {
    lineNumbers.iterate(v);
  }
  public Type resolveType(Type containingType, Type targetType, ResolveListener listener, String detail) {
    BasicType basicTargetType = (BasicType) targetType;
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(state == CONSTRUCTION_STATE, "wrong state");
    }
    if (basicTargetType.isLazy()) {
      BasicType resolved = (BasicType) lazyTypeMap.get(((LazyType) targetType).getKey());
      if (resolved == null) {
        listener.resolveFailed(containingType, (LazyType) targetType, detail + " because target type was not found");
        return targetType;
      }
      if (resolved.isLazy()) {
        if (resolved.isConst() || resolved.isVolatile()) {
          resolved = (BasicType) resolved.resolveTypes(this, listener);
        }
        if (resolved.isLazy()) {
          listener.resolveFailed(containingType, (LazyType) targetType,
                                 detail + " because target type (with key " +
                                 ((Integer) ((LazyType) resolved).getKey()).intValue() +
                                 (resolved.isConst() ? ", const" : ", not const") +
                                 (resolved.isVolatile() ? ", volatile" : ", not volatile") +
                                 ") was lazy");
        }
      }
      return resolved;
    }
    return targetType;
  }
  public Type resolveType(Sym containingSymbol, Type targetType, ResolveListener listener, String detail) {
    BasicType basicTargetType = (BasicType) targetType;
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(state == CONSTRUCTION_STATE, "wrong state");
    }
    if (basicTargetType.isLazy()) {
      BasicType resolved = (BasicType) lazyTypeMap.get(((LazyType) targetType).getKey());
      if (resolved == null) {
        listener.resolveFailed(containingSymbol, (LazyType) targetType, detail);
        return targetType;
      }
      if (resolved.isLazy()) {
        if (resolved.isConst() || resolved.isVolatile()) {
          resolved = (BasicType) resolved.resolveTypes(this, listener);
        }
        if (resolved.isLazy()) {
          listener.resolveFailed(containingSymbol, (LazyType) targetType, detail);
        }
      }
      return resolved;
    }
    return targetType;
  }
  public Sym resolveSym(Sym containingSymbol, Sym targetSym, ResolveListener listener, String detail) {
    if (targetSym == null) return null;
    BasicSym basicTargetSym = (BasicSym) targetSym;
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(state == CONSTRUCTION_STATE, "wrong state");
    }
    if (basicTargetSym.isLazy()) {
      BasicSym resolved = (BasicSym) lazySymMap.get(((LazyBlockSym) targetSym).getKey());
      if (resolved == null) {
        listener.resolveFailed(containingSymbol, (LazyBlockSym) targetSym, detail);
        return targetSym;
      }
      if (resolved.isLazy()) {
        listener.resolveFailed(containingSymbol, (LazyBlockSym) targetSym, detail);
      }
      return resolved;
    }
    return targetSym;
  }
  private void resolveLazyMap(ResolveListener listener) {
    for (Iterator iter = lazyTypeMap.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry entry = (Map.Entry) iter.next();
      BasicType t = (BasicType) entry.getValue();
      BasicType t2 = (BasicType) t.resolveTypes(this, listener);
      if (t2 != t) {
        entry.setValue(t2);
      }
    }
  }
  private BlockSym searchBlocks(Address addr, int lowIdx, int highIdx) {
    if (highIdx < lowIdx) return null;
    if ((lowIdx == highIdx) || (lowIdx == highIdx - 1)) {
      Address lastAddr = null;
      BlockSym ret = null;
      for (int i = highIdx; i >= 0; --i) {
        BlockSym block = (BlockSym) blocks.get(i);
        if (AddressOps.lte(block.getAddress(), addr)) {
          if ((lastAddr == null) || (AddressOps.equal(block.getAddress(), lastAddr))) {
            lastAddr = block.getAddress();
            ret = block;
          } else {
            break;
          }
        }
      }
      return ret;
    }
    int midIdx = (lowIdx + highIdx) >> 1;
    BlockSym block = (BlockSym) blocks.get(midIdx);
    if (AddressOps.lte(block.getAddress(), addr)) {
      return searchBlocks(addr, midIdx, highIdx);
    } else {
      return searchBlocks(addr, lowIdx, midIdx);
    }
  }
}
