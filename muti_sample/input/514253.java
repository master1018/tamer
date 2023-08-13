public class ApplicationAttributeDescriptor extends TextAttributeDescriptor {
    public ApplicationAttributeDescriptor(String xmlLocalName, String uiName,
            String nsUri, String tooltip) {
        super(xmlLocalName, uiName, nsUri, tooltip);
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiClassAttributeNode("android.app.Application", 
                null , false , this, uiParent,
                true );
    }
}
