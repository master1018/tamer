public class SeparatorAttributeDescriptor extends AttributeDescriptor {
    public SeparatorAttributeDescriptor(String label) {
        super(label , null );
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiSeparatorAttributeNode(this, uiParent);
    }
}
