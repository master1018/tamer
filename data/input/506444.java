public class ElementDescriptor {
    private String mXmlName;
    private String mUiName;
    private AttributeDescriptor[] mAttributes;
    private ElementDescriptor[] mChildren;
    private String mTooltip;
    private String mSdkUrl;
    private boolean mMandatory;
    public ElementDescriptor(String xml_name, String ui_name, String tooltip, String sdk_url,
            AttributeDescriptor[] attributes,
            ElementDescriptor[] children,
            boolean mandatory) {
        mMandatory = mandatory;
        mXmlName = xml_name;
        mUiName = ui_name;
        mTooltip = (tooltip != null && tooltip.length() > 0) ? tooltip : null;
        mSdkUrl = (sdk_url != null && sdk_url.length() > 0) ? sdk_url : null;
        setAttributes(attributes != null ? attributes : new AttributeDescriptor[]{});
        mChildren = children != null ? children : new ElementDescriptor[]{};
    }
    public ElementDescriptor(String xml_name, ElementDescriptor[] children, boolean mandatory) {
        this(xml_name, prettyName(xml_name), null, null, null, children, mandatory);
    }
    public ElementDescriptor(String xml_name, ElementDescriptor[] children) {
        this(xml_name, prettyName(xml_name), null, null, null, children, false);
    }
    public ElementDescriptor(String xml_name) {
        this(xml_name, prettyName(xml_name), null, null, null, null, false);
    }
    public boolean isMandatory() {
        return mMandatory;
    }
    public final String getXmlLocalName() {
        int pos = mXmlName.indexOf(':');
        if (pos != -1) {
            return mXmlName.substring(pos+1);
        }
        return mXmlName;
    }
    public String getXmlName() {
        return mXmlName;
    }
    public final String getNamespace() {
        if (mXmlName.startsWith("android:")) { 
            return SdkConstants.NS_RESOURCES;
        }
        return ""; 
    }
    public String getUiName() {
        return mUiName;
    }
    public Image getIcon() {
        IconFactory factory = IconFactory.getInstance();
        int color = hasChildren() ? IconFactory.COLOR_BLUE : IconFactory.COLOR_GREEN;
        int shape = hasChildren() ? IconFactory.SHAPE_RECT : IconFactory.SHAPE_CIRCLE;
        Image icon = factory.getIcon(mXmlName, color, shape);
        return icon != null ? icon : AdtPlugin.getAndroidLogo();
    }
    public ImageDescriptor getImageDescriptor() {
        IconFactory factory = IconFactory.getInstance();
        int color = hasChildren() ? IconFactory.COLOR_BLUE : IconFactory.COLOR_GREEN;
        int shape = hasChildren() ? IconFactory.SHAPE_RECT : IconFactory.SHAPE_CIRCLE;
        ImageDescriptor id = factory.getImageDescriptor(mXmlName, color, shape);
        return id != null ? id : AdtPlugin.getAndroidLogoDesc();
    }
    public AttributeDescriptor[] getAttributes() {
        return mAttributes;
    }
    public void setAttributes(AttributeDescriptor[] attributes) {
        mAttributes = attributes;
        for (AttributeDescriptor attribute : attributes) {
            attribute.setParent(this);
        }
    }
    public ElementDescriptor[] getChildren() {
        return mChildren;
    }
    public boolean hasChildren() {
        return mChildren.length > 0;
    }
    public void setChildren(ElementDescriptor[] newChildren) {
        mChildren = newChildren;
    }
    public void setChildren(Collection<ElementDescriptor> newChildren) {
        setChildren(newChildren.toArray(new ElementDescriptor[newChildren.size()]));
    }
    public String getTooltip() {
        return mTooltip;
    }
    public String getSdkUrl() {
        return mSdkUrl;
    }
    public void setTooltip(String tooltip) {
        mTooltip = tooltip;
    }
    public void setSdkUrl(String sdkUrl) {
        mSdkUrl = sdkUrl;
    }
    public UiElementNode createUiNode() {
        return new UiElementNode(this);
    }
    public ElementDescriptor findChildrenDescriptor(String element_name, boolean recursive) {
        return findChildrenDescriptorInternal(element_name, recursive, null);
    }
    private ElementDescriptor findChildrenDescriptorInternal(String element_name,
            boolean recursive,
            Set<ElementDescriptor> visited) {
        if (recursive && visited == null) {
            visited = new HashSet<ElementDescriptor>();
        }
        for (ElementDescriptor e : getChildren()) {
            if (e.getXmlName().equals(element_name)) {
                return e;
            }
        }
        if (visited != null) {
            visited.add(this);
        }
        if (recursive) {
            for (ElementDescriptor e : getChildren()) {
                if (visited != null) {
                    if (!visited.add(e)) {  
                        continue;
                    }
                }
                ElementDescriptor f = e.findChildrenDescriptorInternal(element_name,
                        recursive, visited);
                if (f != null) {
                    return f;
                }
            }
        }
        return null;
    }
    private static String prettyName(String xml_name) {
        char c[] = xml_name.toCharArray();
        if (c.length > 0) {
            c[0] = Character.toUpperCase(c[0]);
        }
        return new String(c).replace("-", " ");  
    }
}
