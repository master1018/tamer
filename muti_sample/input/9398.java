public abstract class DSO implements LoadObject {
    private MemoizedObject file;  
    private String         filename;
    private Address        addr;
    private long           size;
    private IsDSO          dso = new IsDSO();
    class IsDSO extends MemoizedBoolean {
        protected boolean computeValue() {
           return getFile().getHeader().getFileType() == ELFHeader.FT_DYN;
        }
    };
    class ELFFileByName extends MemoizedObject {
        protected Object computeValue() {
           return ELFFileParser.getParser().parse(DSO.this.filename);
        }
    };
    class ELFFileByAddress extends MemoizedObject {
        protected Object computeValue() {
           return ELFFileParser.getParser().parse(new AddressDataSource(DSO.this.addr));
        }
    };
    public DSO(String filename, long size, Address relocation) throws ELFException {
        this.filename = filename;
        this.size = size;
        this.addr = relocation;
        this.file = new ELFFileByName();
    }
    public DSO(long size, Address relocation) throws ELFException {
        this.addr = relocation;
        this.size = size;
        this.file = new ELFFileByAddress();
    }
    public String getName() {
        return filename;
    }
    public Address getBase() {
        return addr;
    }
    public void setBase(Address newBase) {
        addr = newBase;
        if (filename == null) {
            file = new ELFFileByAddress();
            dso  = new IsDSO();
        }
    }
    public long getSize() {
        return size;
    }
    public CDebugInfoDataBase getDebugInfoDataBase() throws DebuggerException {
        return null;
    }
    public BlockSym debugInfoForPC(Address pc) throws DebuggerException  {
        return null;
    }
    public ClosestSymbol closestSymbolToPC(Address pcAsAddr) throws DebuggerException {
        boolean dso = isDSO();
        long offset = dso? pcAsAddr.minus(addr) : getAddressValue(pcAsAddr);
        ELFSymbol sym = getFile().getHeader().getELFSymbol(offset);
        return (sym != null)? createClosestSymbol(sym.getName(), offset - sym.getValue()) : null;
    }
    public LineNumberInfo lineNumberForPC(Address pc) throws DebuggerException {
        return null;
    }
    public boolean isDSO() {
        return dso.getValue();
    }
    public Address lookupSymbol(String symbol) throws ELFException {
        ELFSymbol sym = getFile().getHeader().getELFSymbol(symbol);
        if (sym == null) {
           return null;
        }
        long value = sym.getValue();
        if (isDSO()) {
           return addr.addOffsetTo(value);
        } else {
           return newAddress(value);
        }
    }
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DSO)) {
           return false;
        }
        DSO other = (DSO)o;
        return getBase().equals(other.getBase());
    }
    public int hashCode() {
        return getBase().hashCode();
    }
    protected ELFFile getFile() {
        return (ELFFile) file.getValue();
    }
    protected abstract Address newAddress(long addr);
    protected abstract long getAddressValue(Address addr);
    protected ClosestSymbol createClosestSymbol(String name, long diff) {
        return new ClosestSymbol(name, diff);
    }
}
