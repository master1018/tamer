public final class ProtoIdsSection extends UniformItemSection {
    private final TreeMap<Prototype, ProtoIdItem> protoIds;
    public ProtoIdsSection(DexFile file) {
        super("proto_ids", file, 4);
        protoIds = new TreeMap<Prototype, ProtoIdItem>();
    }
    @Override
    public Collection<? extends Item> items() {
        return protoIds.values();
    }
    @Override
    public IndexedItem get(Constant cst) {
        throw new UnsupportedOperationException("unsupported");
    }
    public void writeHeaderPart(AnnotatedOutput out) {
        throwIfNotPrepared();
        int sz = protoIds.size();
        int offset = (sz == 0) ? 0 : getFileOffset();
        if (sz > 65536) {
            throw new UnsupportedOperationException("too many proto ids");
        }
        if (out.annotates()) {
            out.annotate(4, "proto_ids_size:  " + Hex.u4(sz));
            out.annotate(4, "proto_ids_off:   " + Hex.u4(offset));
        }
        out.writeInt(sz);
        out.writeInt(offset);
    }
    public ProtoIdItem intern(Prototype prototype) {
        if (prototype == null) {
            throw new NullPointerException("prototype == null");
        }
        throwIfPrepared();
        ProtoIdItem result = protoIds.get(prototype);
        if (result == null) {
            result = new ProtoIdItem(prototype);
            protoIds.put(prototype, result);
        }
        return result;
    }
    public int indexOf(Prototype prototype) {
        if (prototype == null) {
            throw new NullPointerException("prototype == null");
        }
        throwIfNotPrepared();
        ProtoIdItem item = protoIds.get(prototype);
        if (item == null) {
            throw new IllegalArgumentException("not found");
        }
        return item.getIndex();
    }
    @Override
    protected void orderItems() {
        int idx = 0;
        for (Object i : items()) {
            ((ProtoIdItem) i).setIndex(idx);
            idx++;
        }
    }
}
