public class FlagAttributeDescriptor extends TextAttributeDescriptor {
    private String[] mNames;
    public FlagAttributeDescriptor(String xmlLocalName, String uiName, String nsUri,
            String tooltip) {
        super(xmlLocalName, uiName, nsUri, tooltip);
    }
    public FlagAttributeDescriptor(String xmlLocalName, String uiName, String nsUri,
            String tooltip, String[] names) {
       super(xmlLocalName, uiName, nsUri, tooltip);
       mNames = names;
    }
    public String[] getNames() {
        return mNames;
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiFlagAttributeNode(this, uiParent);
    }
    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new FlagValueCellEditor(parent);
    }
}
