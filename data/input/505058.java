public final class TypeIdsSection extends UniformItemSection {
    private final TreeMap<Type, TypeIdItem> typeIds;
    public TypeIdsSection(DexFile file) {
        super("type_ids", file, 4);
        typeIds = new TreeMap<Type, TypeIdItem>();
    }
    @Override
    public Collection<? extends Item> items() {
        return typeIds.values();
    }
    @Override
    public IndexedItem get(Constant cst) {
        if (cst == null) {
            throw new NullPointerException("cst == null");
        }
        throwIfNotPrepared();
        Type type = ((CstType) cst).getClassType();
        IndexedItem result = typeIds.get(type);
        if (result == null) {
            throw new IllegalArgumentException("not found: " + cst);
        }
        return result;
    }
    public void writeHeaderPart(AnnotatedOutput out) {
        throwIfNotPrepared();
        int sz = typeIds.size();
        int offset = (sz == 0) ? 0 : getFileOffset();
        if (sz > 65536) {
            throw new UnsupportedOperationException("too many type ids");
        }
        if (out.annotates()) {
            out.annotate(4, "type_ids_size:   " + Hex.u4(sz));
            out.annotate(4, "type_ids_off:    " + Hex.u4(offset));
        }
        out.writeInt(sz);
        out.writeInt(offset);
    }
    public TypeIdItem intern(Type type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        throwIfPrepared();
        TypeIdItem result = typeIds.get(type);
        if (result == null) {
            result = new TypeIdItem(new CstType(type));
            typeIds.put(type, result);
        }
        return result;
    }
    public TypeIdItem intern(CstType type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        throwIfPrepared();
        Type typePerSe = type.getClassType();
        TypeIdItem result = typeIds.get(typePerSe);
        if (result == null) {
            result = new TypeIdItem(type);
            typeIds.put(typePerSe, result);
        }
        return result;
    }
    public int indexOf(Type type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        throwIfNotPrepared();
        TypeIdItem item = typeIds.get(type);
        if (item == null) {
            throw new IllegalArgumentException("not found: " + type);
        }
        return item.getIndex();
    }
    public int indexOf(CstType type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        return indexOf(type.getClassType());
    }
    @Override
    protected void orderItems() {
        int idx = 0;
        for (Object i : items()) {
            ((TypeIdItem) i).setIndex(idx);
            idx++;
        }
    }
}
