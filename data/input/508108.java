public final class ThemeAttributeDescriptor extends TextAttributeDescriptor {
    public ThemeAttributeDescriptor(String xmlLocalName, String uiName, String nsUri,
            String tooltip) {
        super(xmlLocalName, uiName, nsUri, tooltip);
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiResourceAttributeNode(ResourceType.STYLE, this, uiParent);
    }
}
