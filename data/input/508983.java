public final class CstFieldRef extends CstMemberRef {
    public static CstFieldRef forPrimitiveType(Type primitiveType) {
        return new CstFieldRef(CstType.forBoxedPrimitiveType(primitiveType),
                CstNat.PRIMITIVE_TYPE_NAT);
    }
    public CstFieldRef(CstType definingClass, CstNat nat) {
        super(definingClass, nat);
    }
    @Override
    public String typeName() {
        return "field";
    }
    public Type getType() {
        return getNat().getFieldType();
    }
    @Override
    protected int compareTo0(Constant other) {
        int cmp = super.compareTo0(other);
        if (cmp != 0) {
            return cmp;
        }
        CstFieldRef otherField = (CstFieldRef) other;
        CstUtf8 thisDescriptor = getNat().getDescriptor();
        CstUtf8 otherDescriptor = otherField.getNat().getDescriptor();
        return thisDescriptor.compareTo(otherDescriptor);
    }
}
