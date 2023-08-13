public final class FieldIdItem extends MemberIdItem {
    public FieldIdItem(CstFieldRef field) {
        super(field);
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_FIELD_ID_ITEM;
    }
    @Override
    public void addContents(DexFile file) {
        super.addContents(file);
        TypeIdsSection typeIds = file.getTypeIds();
        typeIds.intern(getFieldRef().getType());
    }
    public CstFieldRef getFieldRef() {
        return (CstFieldRef) getRef();
    }
    @Override
    protected int getTypoidIdx(DexFile file) {
        TypeIdsSection typeIds = file.getTypeIds();
        return typeIds.indexOf(getFieldRef().getType());
    }
    @Override
    protected String getTypoidName() {
        return "type_idx";
    }    
}
