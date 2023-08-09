public abstract class UiAttributeNode {
    private AttributeDescriptor mDescriptor;
    private UiElementNode mUiParent;
    private boolean mIsDirty;
    private boolean mHasError;
    public UiAttributeNode(AttributeDescriptor attributeDescriptor, UiElementNode uiParent) {
        mDescriptor = attributeDescriptor;
        mUiParent = uiParent;
    }
    public final AttributeDescriptor getDescriptor() {
        return mDescriptor;
    }
    public final UiElementNode getUiParent() {
        return mUiParent;
    }
    public abstract String getCurrentValue();
    public final boolean isDirty() {
        return mIsDirty;
    }
    public void setDirty(boolean isDirty) {
        boolean old_value = mIsDirty;
        mIsDirty = isDirty;
        if (old_value != isDirty) {
            getUiParent().getEditor().editorDirtyStateChanged();
        }
    }
    public final void setHasError(boolean errorFlag) {
        mHasError = errorFlag;
    }
    public final boolean hasError() {
        return mHasError;
    }
    public abstract void createUiControl(Composite parent, IManagedForm managedForm);
    public abstract String[] getPossibleValues(String prefix);
    public abstract void updateValue(Node xml_attribute_node);
    public abstract void commit();
}
