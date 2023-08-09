final  class MethodListParser extends MemberListParser {
    final private StdMethodList methods;
    public MethodListParser(DirectClassFile cf, CstType definer,
            int offset, AttributeFactory attributeFactory) {
        super(cf, definer, offset, attributeFactory);
        methods = new StdMethodList(getCount());
    }
    public StdMethodList getList() {
        parseIfNecessary();
        return methods;
    }
    @Override
    protected String humanName() {
        return "method";
    }
    @Override
    protected String humanAccessFlags(int accessFlags) {
        return AccessFlags.methodString(accessFlags);
    }
    @Override
    protected int getAttributeContext() {
        return AttributeFactory.CTX_METHOD;
    }
    @Override
    protected Member set(int n, int accessFlags, CstNat nat,
                         AttributeList attributes) {
        StdMethod meth =
            new StdMethod(getDefiner(), accessFlags, nat, attributes);
        methods.set(n, meth);
        return meth;
    }
}
