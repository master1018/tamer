public final class StdMethod extends StdMember implements Method {
    private final Prototype effectiveDescriptor;
    public StdMethod(CstType definingClass, int accessFlags, CstNat nat,
            AttributeList attributes) {
        super(definingClass, accessFlags, nat, attributes);
        String descStr = getDescriptor().getString();
        effectiveDescriptor =
            Prototype.intern(descStr, definingClass.getClassType(),
                                    AccessFlags.isStatic(accessFlags),
                                    nat.isInstanceInit());
    }
    public Prototype getEffectiveDescriptor() {
        return effectiveDescriptor;
    }
}
