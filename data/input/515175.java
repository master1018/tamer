public class UiSeparatorAttributeNode extends UiAttributeNode {
    public UiSeparatorAttributeNode(SeparatorAttributeDescriptor attrDesc,
            UiElementNode uiParent) {
        super(attrDesc, uiParent);
    }
    @Override
    public String getCurrentValue() {
        return null;
    }
    @Override
    public void setDirty(boolean isDirty) {
    }
    @Override
    public void createUiControl(Composite parent, IManagedForm managedForm) {
        FormToolkit toolkit = managedForm.getToolkit();
        Composite row = toolkit.createComposite(parent);
        TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
        if (parent.getLayout() instanceof TableWrapLayout) {
            twd.colspan = ((TableWrapLayout) parent.getLayout()).numColumns;
        }
        row.setLayoutData(twd);
        row.setLayout(new GridLayout(3, false ));
        Label sep = toolkit.createSeparator(row, SWT.HORIZONTAL);
        GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.widthHint = 16;
        sep.setLayoutData(gd);
        Label label = toolkit.createLabel(row, getDescriptor().getXmlLocalName());
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        sep = toolkit.createSeparator(row, SWT.HORIZONTAL);
        sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }
    @Override
    public String[] getPossibleValues(String prefix) {
        return null;
    }
    @Override
    public void updateValue(Node xml_attribute_node) {
    }
    @Override
    public void commit() {
    }
}
