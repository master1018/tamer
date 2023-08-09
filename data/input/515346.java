public class ItemElementDescriptor extends ElementDescriptor {
    public ItemElementDescriptor(String xml_name, String ui_name,
            String tooltip, String sdk_url, AttributeDescriptor[] attributes,
            ElementDescriptor[] children, boolean mandatory) {
        super(xml_name, ui_name, tooltip, sdk_url, attributes, children, mandatory);
    }
    @Override
    public UiElementNode createUiNode() {
        return new UiItemElementNode(this);
    }
}
