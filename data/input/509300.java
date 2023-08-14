public class ResourceValueCellEditor extends EditableDialogCellEditor {
    private UiResourceAttributeNode mUiResourceAttribute;
    public ResourceValueCellEditor(Composite parent) {
        super(parent);
    }
    @Override
    protected Object openDialogBox(Control cellEditorWindow) {
        if (mUiResourceAttribute != null) {
            String currentValue = (String)getValue();
            return mUiResourceAttribute.showDialog(cellEditorWindow.getShell(), currentValue);
        }
        return null;
    }
    @Override
    protected void doSetValue(Object value) {
        if (value instanceof UiResourceAttributeNode) {
            mUiResourceAttribute = (UiResourceAttributeNode)value;
            super.doSetValue(mUiResourceAttribute.getCurrentValue());
            return;
        }
        super.doSetValue(value);
    }
}
