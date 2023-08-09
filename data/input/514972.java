public final class StdField extends StdMember implements Field {
    public StdField(CstType definingClass, int accessFlags, CstNat nat,
                    AttributeList attributes) {
        super(definingClass, accessFlags, nat, attributes);
    }
    public TypedConstant getConstantValue() {
        AttributeList attribs = getAttributes();
        AttConstantValue cval = (AttConstantValue)
            attribs.findFirst(AttConstantValue.ATTRIBUTE_NAME);
        if (cval == null) {
            return null;
        }
        return cval.getConstantValue();
    }
}
