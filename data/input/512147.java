public class FlagValueCellEditor extends EditableDialogCellEditor {
    private UiFlagAttributeNode mUiFlagAttribute;
    public FlagValueCellEditor(Composite parent) {
        super(parent);
    }
    @Override
    protected Object openDialogBox(Control cellEditorWindow) {
        if (mUiFlagAttribute != null) {
            String currentValue = (String)getValue();
            return mUiFlagAttribute.showDialog(cellEditorWindow.getShell(), currentValue);
        }
        return null;
    }
    @Override
    protected void doSetValue(Object value) {
        if (value instanceof UiFlagAttributeNode) {
            mUiFlagAttribute = (UiFlagAttributeNode)value;
            super.doSetValue(mUiFlagAttribute.getCurrentValue());
            return;
        }
        super.doSetValue(value);
    }
}
