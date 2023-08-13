public abstract class AttributeDescriptor {
    private String mXmlLocalName;
    private ElementDescriptor mParent;
    private final String mNsUri;
    private boolean mDeprecated;
    public AttributeDescriptor(String xmlLocalName, String nsUri) {
        mXmlLocalName = xmlLocalName;
        mNsUri = nsUri;
    }
    public final String getXmlLocalName() {
        return mXmlLocalName;
    }
    public final String getNamespaceUri() {
        return mNsUri;
    }
    final void setParent(ElementDescriptor parent) {
        mParent = parent;
    }
    public final ElementDescriptor getParent() {
        return mParent;
    }
    public void setDeprecated(boolean isDeprecated) {
        mDeprecated = isDeprecated;
    }
    public boolean isDeprecated() {
        return mDeprecated;
    }
    public Image getIcon() {
        IconFactory factory = IconFactory.getInstance();
        Image icon;
        icon = factory.getIcon(getXmlLocalName(), IconFactory.COLOR_RED, IconFactory.SHAPE_CIRCLE);
        return icon != null ? icon : AdtPlugin.getAndroidLogo();
    }
    public abstract UiAttributeNode createUiNode(UiElementNode uiParent);
}    
