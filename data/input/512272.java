public class ListValueCellEditor extends ComboBoxCellEditor {
    private String[] mItems;
    private CCombo mCombo;
    public ListValueCellEditor(Composite parent) {
        super(parent, new String[0], SWT.DROP_DOWN);
    }
    @Override
    protected Control createControl(Composite parent) {
        mCombo = (CCombo) super.createControl(parent);
        return mCombo;
    }
    @Override
    protected void doSetValue(Object value) {
        if (value instanceof UiListAttributeNode) {
            UiListAttributeNode uiListAttribute = (UiListAttributeNode)value;
            String[] items = uiListAttribute.getPossibleValues(null);
            mItems = new String[items.length];
            System.arraycopy(items, 0, mItems, 0, items.length);
            setItems(mItems);
            String attrValue = uiListAttribute.getCurrentValue();
            mCombo.setText(attrValue);
            return;
        }
        super.doSetValue(value);
    }
    @Override
    protected Object doGetValue() {
        String comboText = mCombo.getText();
        if (comboText == null) {
            return ""; 
        }
        return comboText;
    }
}
