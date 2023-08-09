public final class MethodIdItem extends MemberIdItem {
    public MethodIdItem(CstBaseMethodRef method) {
        super(method);
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_METHOD_ID_ITEM;
    }
    @Override
    public void addContents(DexFile file) {
        super.addContents(file);
        ProtoIdsSection protoIds = file.getProtoIds();
        protoIds.intern(getMethodRef().getPrototype());
    }
    public CstBaseMethodRef getMethodRef() {
        return (CstBaseMethodRef) getRef();
    }
    @Override
    protected int getTypoidIdx(DexFile file) {
        ProtoIdsSection protoIds = file.getProtoIds();
        return protoIds.indexOf(getMethodRef().getPrototype());
    }
    @Override
    protected String getTypoidName() {
        return "proto_idx";
    }    
}
