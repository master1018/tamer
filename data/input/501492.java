public class TextValueCellEditor extends TextCellEditor {
    public TextValueCellEditor(Composite parent) {
        super(parent);
    }
    @Override
    protected void doSetValue(Object value) {
        if (value instanceof UiAttributeNode) {
            super.doSetValue(((UiAttributeNode)value).getCurrentValue());
            return;
        }
        super.doSetValue(value);
    }
}
