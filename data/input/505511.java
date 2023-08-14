public class ManifestPkgAttrDescriptor extends TextAttributeDescriptor {
    public ManifestPkgAttrDescriptor(String xmlLocalName, String uiName, String nsUri,
            String tooltip) {
        super(xmlLocalName, uiName, nsUri, tooltip);
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiManifestPkgAttrNode(this, uiParent);
    }
}
