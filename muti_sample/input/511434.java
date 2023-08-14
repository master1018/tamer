public final class ViewElementDescriptor extends ElementDescriptor {
    private String mFullClassName;
    private AttributeDescriptor[] mLayoutAttributes;
    private ViewElementDescriptor mSuperClassDesc;
    public ViewElementDescriptor(String xml_name, String ui_name,
            String fullClassName,
            String tooltip, String sdk_url,
            AttributeDescriptor[] attributes, AttributeDescriptor[] layoutAttributes,
            ElementDescriptor[] children, boolean mandatory) {
        super(xml_name, ui_name, tooltip, sdk_url, attributes, children, mandatory);
        mFullClassName = fullClassName;
        mLayoutAttributes = layoutAttributes != null ? layoutAttributes : new AttributeDescriptor[0];
    }
    public ViewElementDescriptor(String xml_name, String fullClassName,
            ElementDescriptor[] children,
            boolean mandatory) {
        super(xml_name, children, mandatory);
        mFullClassName = fullClassName;
    }
    public ViewElementDescriptor(String xml_name, String fullClassName,
            ElementDescriptor[] children) {
        super(xml_name, children);
        mFullClassName = fullClassName;
    }
    public ViewElementDescriptor(String xml_name, String fullClassName) {
        super(xml_name);
        mFullClassName = fullClassName;
    }
    public String getFullClassName() {
        return mFullClassName;
    }
    public AttributeDescriptor[] getLayoutAttributes() {
        return mLayoutAttributes;
    }
    @Override
    public UiElementNode createUiNode() {
        return new UiViewElementNode(this);
    }
    public ViewElementDescriptor getSuperClassDesc() {
        return mSuperClassDesc;
    }
    public void setSuperClass(ViewElementDescriptor superClassDesc) {
        mSuperClassDesc = superClassDesc;
    }
}
