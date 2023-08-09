public final class XmlnsAttributeDescriptor extends AttributeDescriptor {
    public final static String XMLNS_URI = "http:
    private String mValue;
    public XmlnsAttributeDescriptor(String defaultPrefix, String value) {
        super(defaultPrefix, XMLNS_URI);
        mValue = value;
    }
    public String getValue() {
        return mValue;
    }
    public String getXmlNsPrefix() {
        return "xmlns"; 
    }
    public String getXmlNsName() {
        return getXmlNsPrefix() + ":" + getXmlLocalName(); 
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return null;
    }
}
