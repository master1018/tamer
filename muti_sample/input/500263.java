public final class CstInterfaceMethodRef
        extends CstBaseMethodRef {
    private CstMethodRef methodRef;
    public CstInterfaceMethodRef(CstType definingClass, CstNat nat) {
        super(definingClass, nat);
        methodRef = null;
    }
    @Override
    public String typeName() {
        return "ifaceMethod";
    }
    public CstMethodRef toMethodRef() {
        if (methodRef == null) {
            methodRef = new CstMethodRef(getDefiningClass(), getNat());
        }
        return methodRef;
    }
}
