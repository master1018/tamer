public class EnumAttributeDescriptor extends ListAttributeDescriptor {
    public EnumAttributeDescriptor(String xmlLocalName, String uiName, String nsUri,
            String tooltip) {
        super(xmlLocalName, uiName, nsUri, tooltip);
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiListAttributeNode(this, uiParent);
    }
}
