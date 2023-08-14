public class TextAttributeDescriptor extends AttributeDescriptor implements IPropertyDescriptor {
    private String mUiName;
    private String mTooltip;
    public TextAttributeDescriptor(String xmlLocalName, String uiName,
            String nsUri, String tooltip) {
        super(xmlLocalName, nsUri);
        mUiName = uiName;
        mTooltip = (tooltip != null && tooltip.length() > 0) ? tooltip : null;
    }
    public final String getUiName() {
        return mUiName;
    }
    public final String getTooltip() {
        return mTooltip;
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiTextAttributeNode(this, uiParent);
    }
    public CellEditor createPropertyEditor(Composite parent) {
        return new TextValueCellEditor(parent);
    }
    public String getCategory() {
        if (isDeprecated()) {
            return "Deprecated";
        }
        ElementDescriptor parent = getParent();
        if (parent != null) {
            return parent.getUiName();
        }
        return null;
    }
    public String getDescription() {
        return mTooltip;
    }
    public String getDisplayName() {
        return mUiName;
    }
    public String[] getFilterFlags() {
        return null;
    }
    public Object getHelpContextIds() {
        return null;
    }
    public Object getId() {
        return this;
    }
    public ILabelProvider getLabelProvider() {
        return AttributeDescriptorLabelProvider.getProvider();
    }
    public boolean isCompatibleWith(IPropertyDescriptor anotherProperty) {
        return anotherProperty == this;
    }
}
