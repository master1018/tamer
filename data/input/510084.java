public final class ColorValueDescriptor extends TextValueDescriptor {
    public ColorValueDescriptor(String uiName, String tooltip) {
        super(uiName, tooltip);
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiColorValueNode(this, uiParent);
    }
}
