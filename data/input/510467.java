public class PackageAttributeDescriptor extends TextAttributeDescriptor {
    public PackageAttributeDescriptor(String xmlLocalName, String uiName, String nsUri,
            String tooltip) {
        super(xmlLocalName, uiName, nsUri, tooltip);
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiPackageAttributeNode(this, uiParent);
    }
}
