public final class CstEnumRef extends CstMemberRef {
    private CstFieldRef fieldRef;
    public CstEnumRef(CstNat nat) {
        super(new CstType(nat.getFieldType()), nat);
        fieldRef = null;
    }
    @Override
    public String typeName() {
        return "enum";
    }
    public Type getType() {
        return getDefiningClass().getClassType();
    }
    public CstFieldRef getFieldRef() {
        if (fieldRef == null) {
            fieldRef = new CstFieldRef(getDefiningClass(), getNat());
        }
        return fieldRef;
    }
}
