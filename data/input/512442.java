public class UiListAttributeNode extends UiAbstractTextAttributeNode {
    protected Combo mCombo;
    public UiListAttributeNode(ListAttributeDescriptor attributeDescriptor,
            UiElementNode uiParent) {
        super(attributeDescriptor, uiParent);
    }
    @Override
    public final void createUiControl(final Composite parent, IManagedForm managedForm) {
        FormToolkit toolkit = managedForm.getToolkit();
        TextAttributeDescriptor desc = (TextAttributeDescriptor) getDescriptor();
        Label label = toolkit.createLabel(parent, desc.getUiName());
        label.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE));
        SectionHelper.addControlTooltip(label, DescriptorsUtils.formatTooltip(desc.getTooltip()));
        int style = SWT.DROP_DOWN;
        mCombo = new Combo(parent, style);
        TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
        twd.maxWidth = 100;
        mCombo.setLayoutData(twd);
        fillCombo();
        setTextWidgetValue(getCurrentValue());
        mCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onComboChange();
            }
        });
        mCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onComboChange();
            }
        });
        mCombo.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                mCombo = null;
            }
        });
    }
    protected void fillCombo() {
        String[] values = getPossibleValues(null);
        if (values == null) {
            AdtPlugin.log(IStatus.ERROR,
                    "FrameworkResourceManager did not provide values yet for %1$s",
                    getDescriptor().getXmlLocalName());
        } else {
            for (String value : values) {
                mCombo.add(value);
            }
        }
    }
    @Override
    public String[] getPossibleValues(String prefix) {
        AttributeDescriptor descriptor = getDescriptor();
        UiElementNode uiParent = getUiParent();
        String attr_name = descriptor.getXmlLocalName();
        String element_name = uiParent.getDescriptor().getXmlName();
        String nsPrefix = "";
        if (SdkConstants.NS_RESOURCES.equals(descriptor.getNamespaceUri())) {
            nsPrefix = "android:"; 
        } else if (XmlnsAttributeDescriptor.XMLNS_URI.equals(descriptor.getNamespaceUri())) {
            nsPrefix = "xmlns:"; 
        }
        attr_name = nsPrefix + attr_name;
        String[] values = null;
        if (descriptor instanceof ListAttributeDescriptor &&
                ((ListAttributeDescriptor) descriptor).getValues() != null) {
            values = ((ListAttributeDescriptor) descriptor).getValues();
        }
        if (values == null) {
            UiElementNode uiNode = getUiParent();
            AndroidEditor editor = uiNode.getEditor();
            AndroidTargetData data = editor.getTargetData();
            if (data != null) {
                UiElementNode grandParentNode = uiParent.getUiParent();
                String greatGrandParentNodeName = null;
                if (grandParentNode != null) {
                    UiElementNode greatGrandParentNode = grandParentNode.getUiParent();
                    if (greatGrandParentNode != null) {
                        greatGrandParentNodeName =
                            greatGrandParentNode.getDescriptor().getXmlName();
                    }
                }
                values = data.getAttributeValues(element_name, attr_name, greatGrandParentNodeName);
            }
        }
        return values;
    }
    @Override
    public String getTextWidgetValue() {
        if (mCombo != null) {
            return mCombo.getText();
        }
        return null;
    }
    @Override
    public final boolean isValid() {
        return mCombo != null;
    }
    @Override
    public void setTextWidgetValue(String value) {
        if (mCombo != null) {
            mCombo.setText(value);
        }
    }
    private void onComboChange() {
        if (!isInInternalTextModification() &&
                !isDirty() &&
                mCombo != null &&
                getCurrentValue() != null &&
                !mCombo.getText().equals(getCurrentValue())) {
            setDirty(true);
        }
    }
}
