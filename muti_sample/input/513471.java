public class ListAttributeDescriptor extends TextAttributeDescriptor {
    private String[] mValues = null;
    public ListAttributeDescriptor(String xmlLocalName, String uiName, String nsUri,
            String tooltip) {
        super(xmlLocalName, uiName, nsUri, tooltip);
    }
    public ListAttributeDescriptor(String xmlLocalName, String uiName, String nsUri, 
            String tooltip, String[] values) {
        super(xmlLocalName, uiName, nsUri, tooltip);
        mValues = values;
    }
    public String[] getValues() {
        return mValues;
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiListAttributeNode(this, uiParent);
    }
    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new ListValueCellEditor(parent);
    }
}
