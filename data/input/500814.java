public class UiTextAttributeNode extends UiAbstractTextAttributeNode {
    private Text mText;
    private IManagedForm mManagedForm;
    public UiTextAttributeNode(AttributeDescriptor attributeDescriptor, UiElementNode uiParent) {
        super(attributeDescriptor, uiParent);
    }
    @Override
    public void createUiControl(Composite parent, IManagedForm managedForm) {
        setManagedForm(managedForm);
        TextAttributeDescriptor desc = (TextAttributeDescriptor) getDescriptor();
        Text text = SectionHelper.createLabelAndText(parent, managedForm.getToolkit(),
                desc.getUiName(), getCurrentValue(),
                DescriptorsUtils.formatTooltip(desc.getTooltip()));
        setTextWidget(text);
    }
    @Override
    public String[] getPossibleValues(String prefix) {
        return null;
    }
    protected void setManagedForm(IManagedForm managedForm) {
         mManagedForm = managedForm;
    }
    protected IManagedForm getManagedForm() {
        return mManagedForm;
    }
    @Override
    public boolean isValid() {
        return mText != null;
    }
    @Override
    public String getTextWidgetValue() {
        if (mText != null) {
            return mText.getText();
        }
        return null;
    }
    @Override
    public void setTextWidgetValue(String value) {
        if (mText != null) {
            mText.setText(value);
        }
    }
    protected final void setTextWidget(Text textWidget) {
        mText = textWidget;
        if (textWidget != null) {
            Object data = textWidget.getLayoutData();
            if (data == null) {
            } else if (data instanceof GridData) {
                ((GridData)data).widthHint = AndroidEditor.TEXT_WIDTH_HINT;
            } else if (data instanceof TableWrapData) {
                ((TableWrapData)data).maxWidth = 100;
            }
            mText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    if (!isInInternalTextModification() &&
                            !isDirty() &&
                            mText != null &&
                            getCurrentValue() != null &&
                            !mText.getText().equals(getCurrentValue())) {
                        setDirty(true);
                    }
                }            
            });
            mText.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    mText = null;
                }
            });
        }
        onAddValidators(mText);
    }
    protected void onAddValidators(Text text) {
    }
    protected final Text getTextWidget() {
        return mText;
    }
}
