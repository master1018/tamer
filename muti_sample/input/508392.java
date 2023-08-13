public final class FieldIdsSection extends MemberIdsSection {
    private final TreeMap<CstFieldRef, FieldIdItem> fieldIds;
    public FieldIdsSection(DexFile file) {
        super("field_ids", file);
        fieldIds = new TreeMap<CstFieldRef, FieldIdItem>();
    }
    @Override
    public Collection<? extends Item> items() {
        return fieldIds.values();
    }
    @Override
    public IndexedItem get(Constant cst) {
        if (cst == null) {
            throw new NullPointerException("cst == null");
        }
        throwIfNotPrepared();
        IndexedItem result = fieldIds.get((CstFieldRef) cst);
        if (result == null) {
            throw new IllegalArgumentException("not found");
        }
        return result;
    }
    public void writeHeaderPart(AnnotatedOutput out) {
        throwIfNotPrepared();
        int sz = fieldIds.size();
        int offset = (sz == 0) ? 0 : getFileOffset();
        if (out.annotates()) {
            out.annotate(4, "field_ids_size:  " + Hex.u4(sz));
            out.annotate(4, "field_ids_off:   " + Hex.u4(offset));
        }
        out.writeInt(sz);
        out.writeInt(offset);
    }
    public FieldIdItem intern(CstFieldRef field) {
        if (field == null) {
            throw new NullPointerException("field == null");
        }
        throwIfPrepared();
        FieldIdItem result = fieldIds.get(field);
        if (result == null) {
            result = new FieldIdItem(field);
            fieldIds.put(field, result);
        }
        return result;
    }
    public int indexOf(CstFieldRef ref) {
        if (ref == null) {
            throw new NullPointerException("ref == null");
        }
        throwIfNotPrepared();
        FieldIdItem item = fieldIds.get(ref);
        if (item == null) {
            throw new IllegalArgumentException("not found");
        }
        return item.getIndex();
    }
}
