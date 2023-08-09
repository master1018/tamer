class DLL implements LoadObject {
  DLL(Win32Debugger dbg, String filename, long size, Address relocation) throws COFFException {
    this.dbg     = dbg;
    fullPathName = filename;
    this.size    = size;
    file = new MemoizedObject() {
        public Object computeValue() {
          return COFFFileParser.getParser().parse(fullPathName);
        }
      };
    addr = relocation;
  }
  DLL(Address base) throws COFFException {
    this.addr = base;
    file = new MemoizedObject() {
        public Object computeValue() {
          return COFFFileParser.getParser().parse(new AddressDataSource(addr));
        }
      };
  }
  boolean isDLL() {
    return getFile().getHeader().hasCharacteristic(Characteristics.IMAGE_FILE_DLL);
  }
  Address lookupSymbol(String symbol) throws COFFException {
    if (!isDLL()) {
      return null;
    }
    ExportDirectoryTable exports = getExportDirectoryTable();
    return lookupSymbol(symbol, exports,
                        0, exports.getNumberOfNamePointers() - 1);
  }
  public Address getBase() {
    return addr;
  }
  public String getName() {
    return fullPathName;
  }
  public long getSize() {
    return size;
  }
  public CDebugInfoDataBase getDebugInfoDataBase() throws DebuggerException {
    if (db != null) {
      return db;
    }
    if (dbg == null) {
      return null; 
    }
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(fullPathName != null, "Need full path name to build debug info database");
    }
    db = new Win32CDebugInfoBuilder(dbg).buildDataBase(fullPathName, addr);
    return db;
  }
  public BlockSym debugInfoForPC(Address pc) throws DebuggerException {
    CDebugInfoDataBase db = getDebugInfoDataBase();
    if (db == null) {
      return null;
    }
    return db.debugInfoForPC(pc);
  }
  public ClosestSymbol closestSymbolToPC(Address pcAsAddr) throws DebuggerException {
    ExportDirectoryTable exports = getExportDirectoryTable();
    if (exports == null) {
      return null;
    }
    String name = null;
    long   pc   = dbg.getAddressValue(pcAsAddr);
    long   diff = Long.MAX_VALUE;
    long   base = dbg.getAddressValue(addr);
    for (int i = 0; i < exports.getNumberOfNamePointers(); i++) {
      if (!exports.isExportAddressForwarder(exports.getExportOrdinal(i))) {
        long tmp = base + (exports.getExportAddress(exports.getExportOrdinal(i)) & 0xFFFFFFFF);
        if ((tmp <= pc) && ((pc - tmp) < diff)) {
          diff = pc - tmp;
          name = exports.getExportName(i);
        }
      }
    }
    if (name == null) {
      return null;
    }
    return new ClosestSymbol(name, diff);
  }
  public LineNumberInfo lineNumberForPC(Address pc) throws DebuggerException {
    CDebugInfoDataBase db = getDebugInfoDataBase();
    if (db == null) {
      return null;
    }
    return db.lineNumberForPC(pc);
  }
  void close() {
    getFile().close();
    file = null;
  }
  private COFFFile getFile() {
    return (COFFFile) file.getValue();
  }
  private Address lookupSymbol(String symbol, ExportDirectoryTable exports,
                               int loIdx, int hiIdx) {
    do {
      int curIdx = ((loIdx + hiIdx) >> 1);
      String cur = exports.getExportName(curIdx);
      if (symbol.equals(cur)) {
        return addr.addOffsetTo(
          ((long) exports.getExportAddress(exports.getExportOrdinal(curIdx))) & 0xFFFFFFFFL
        );
      }
      if (symbol.compareTo(cur) < 0) {
        if (hiIdx == curIdx) {
          hiIdx = curIdx - 1;
        } else {
          hiIdx = curIdx;
        }
      } else {
        if (loIdx == curIdx) {
          loIdx = curIdx + 1;
        } else {
          loIdx = curIdx;
        }
      }
    } while (loIdx <= hiIdx);
    return null;
  }
  private ExportDirectoryTable getExportDirectoryTable() {
    return
      getFile().getHeader().getOptionalHeader().getDataDirectories().getExportDirectoryTable();
  }
  private Win32Debugger  dbg;
  private String         fullPathName;
  private long           size;
  private MemoizedObject file;
  private Address        addr;
  private CDebugInfoDataBase db;
}
