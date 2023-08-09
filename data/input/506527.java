public class TextValueDescriptor extends TextAttributeDescriptor {
    public TextValueDescriptor(String uiName, String tooltip) {
        super("#text" , uiName, null , tooltip);
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiTextValueNode(this, uiParent);
    }
}
