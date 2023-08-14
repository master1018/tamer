public final class StringIdsSection
        extends UniformItemSection {
    private final TreeMap<CstUtf8, StringIdItem> strings;
    public StringIdsSection(DexFile file) {
        super("string_ids", file, 4);
        strings = new TreeMap<CstUtf8, StringIdItem>();
    }
    @Override
    public Collection<? extends Item> items() {
        return strings.values();
    }
    @Override
    public IndexedItem get(Constant cst) {
        if (cst == null) {
            throw new NullPointerException("cst == null");
        }
        throwIfNotPrepared();
        if (cst instanceof CstString) {
            cst = ((CstString) cst).getString();
        }
        IndexedItem result = strings.get((CstUtf8) cst);
        if (result == null) {
            throw new IllegalArgumentException("not found");
        }
        return result;
    }
    public void writeHeaderPart(AnnotatedOutput out) {
        throwIfNotPrepared();
        int sz = strings.size();
        int offset = (sz == 0) ? 0 : getFileOffset();
        if (out.annotates()) {
            out.annotate(4, "string_ids_size: " + Hex.u4(sz));
            out.annotate(4, "string_ids_off:  " + Hex.u4(offset));
        }
        out.writeInt(sz);
        out.writeInt(offset);
    }
    public StringIdItem intern(String string) {
        CstUtf8 utf8 = new CstUtf8(string);
        return intern(new StringIdItem(utf8));
    }
    public StringIdItem intern(CstString string) {
        CstUtf8 utf8 = string.getString();
        return intern(new StringIdItem(utf8));
    }
    public StringIdItem intern(CstUtf8 string) {
        return intern(new StringIdItem(string));
    }
    public StringIdItem intern(StringIdItem string) {
        if (string == null) {
            throw new NullPointerException("string == null");
        }
        throwIfPrepared();
        CstUtf8 value = string.getValue();
        StringIdItem already = strings.get(value);
        if (already != null) {
            return already;
        }
        strings.put(value, string);
        return string;
    }
    public void intern(CstNat nat) {
        intern(nat.getName());
        intern(nat.getDescriptor());
    }
    public int indexOf(CstUtf8 string) {
        if (string == null) {
            throw new NullPointerException("string == null");
        }
        throwIfNotPrepared();
        StringIdItem s = strings.get(string);
        if (s == null) {
            throw new IllegalArgumentException("not found");
        }
        return s.getIndex();
    }
    public int indexOf(CstString string) {
        return indexOf(string.getString());
    }
    @Override
    protected void orderItems() {
        int idx = 0;
        for (StringIdItem s : strings.values()) {
            s.setIndex(idx);
            idx++;
        }
    }
}
