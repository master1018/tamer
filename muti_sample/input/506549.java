public class ManifestElementDescriptor extends ElementDescriptor {
    public ManifestElementDescriptor(String xml_name, String ui_name, String tooltip, String sdk_url,
            AttributeDescriptor[] attributes,
            ElementDescriptor[] children,
            boolean mandatory) {
        super(xml_name, ui_name, tooltip, sdk_url, attributes, children, mandatory);
    }
    public ManifestElementDescriptor(String xml_name, String ui_name, String tooltip, String sdk_url,
            AttributeDescriptor[] attributes,
            ElementDescriptor[] children) {
        super(xml_name, ui_name, tooltip, sdk_url, attributes, children, false);
    }
    public ManifestElementDescriptor(String xml_name, ElementDescriptor[] children) {
        super(xml_name, children);
    }
    public ManifestElementDescriptor(String xml_name) {
        super(xml_name, null);
    }
    @Override
    public UiElementNode createUiNode() {
        return new UiManifestElementNode(this);
    }
}
