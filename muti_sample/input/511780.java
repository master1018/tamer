public abstract class MemberIdItem extends IdItem {
    public static final int WRITE_SIZE = 8;
    private final CstMemberRef cst;
    public MemberIdItem(CstMemberRef cst) {
        super(cst.getDefiningClass());
        this.cst = cst;
    }
    @Override
    public int writeSize() {
        return WRITE_SIZE;
    }
    @Override
    public void addContents(DexFile file) {
        super.addContents(file);
        StringIdsSection stringIds = file.getStringIds();
        stringIds.intern(getRef().getNat().getName());
    }
    @Override
    public final void writeTo(DexFile file, AnnotatedOutput out) {
        TypeIdsSection typeIds = file.getTypeIds();
        StringIdsSection stringIds = file.getStringIds();
        CstNat nat = cst.getNat();
        int classIdx = typeIds.indexOf(getDefiningClass());
        int nameIdx = stringIds.indexOf(nat.getName());
        int typoidIdx = getTypoidIdx(file);
        if (out.annotates()) {
            out.annotate(0, indexString() + ' ' + cst.toHuman());
            out.annotate(2, "  class_idx: " + Hex.u2(classIdx));
            out.annotate(2, String.format("  %-10s %s", getTypoidName() + ':',
                            Hex.u2(typoidIdx)));
            out.annotate(4, "  name_idx:  " + Hex.u4(nameIdx));
        }            
        out.writeShort(classIdx);
        out.writeShort(typoidIdx);
        out.writeInt(nameIdx);
    }
    protected abstract int getTypoidIdx(DexFile file);
    protected abstract String getTypoidName();
    public final CstMemberRef getRef() {
        return cst;
    }
}
