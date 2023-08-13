public final class TypeIdItem extends IdItem {
    public static final int WRITE_SIZE = 4;
    public TypeIdItem(CstType type) {
        super(type);
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_TYPE_ID_ITEM;
    }
    @Override
    public int writeSize() {
        return WRITE_SIZE;
    }
    @Override
    public void addContents(DexFile file) {
        file.getStringIds().intern(getDefiningClass().getDescriptor());
    }
    @Override
    public void writeTo(DexFile file, AnnotatedOutput out) {
        CstType type = getDefiningClass();
        CstUtf8 descriptor = type.getDescriptor();
        int idx = file.getStringIds().indexOf(descriptor);
        if (out.annotates()) {
            out.annotate(0, indexString() + ' ' + descriptor.toHuman());
            out.annotate(4, "  descriptor_idx: " + Hex.u4(idx));
        }
        out.writeInt(idx);
    }
}
