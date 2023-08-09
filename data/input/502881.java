final  class FieldListParser extends MemberListParser {
    private final StdFieldList fields;
    public FieldListParser(DirectClassFile cf, CstType definer, int offset,
            AttributeFactory attributeFactory) {
        super(cf, definer, offset, attributeFactory);
        fields = new StdFieldList(getCount());
    }
    public StdFieldList getList() {
        parseIfNecessary();
        return fields;
    }
    @Override
    protected String humanName() {
        return "field";
    }
    @Override
    protected String humanAccessFlags(int accessFlags) {
        return AccessFlags.fieldString(accessFlags);
    }
    @Override
    protected int getAttributeContext() {
        return AttributeFactory.CTX_FIELD;
    }
    @Override
    protected Member set(int n, int accessFlags, CstNat nat,
                         AttributeList attributes) {
        StdField field =
            new StdField(getDefiner(), accessFlags, nat, attributes);
        fields.set(n, field);
        return field;
    }
}
