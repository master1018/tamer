public class IdResourceItem extends ProjectResourceItem implements IIdResourceItem {
    private final boolean mIsDeclaredInline;
    IdResourceItem(String name, boolean isDeclaredInline) {
        super(name);
        mIsDeclaredInline = isDeclaredInline;
    }
    public boolean isDeclaredInline() {
        return mIsDeclaredInline;
    }
    @Override
    public boolean isEditableDirectly() {
        return !mIsDeclaredInline;
    }
}
